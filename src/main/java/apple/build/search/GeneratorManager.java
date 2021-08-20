package apple.build.search;

import apple.build.query.QuerySaved;
import apple.build.query.QuerySavingService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GeneratorManager implements Runnable {
    private static final Map<TaskType, List<GeneratorTask>> allTasks = new HashMap<>() {{
        for (TaskType type : TaskType.values()) put(type, new ArrayList<>());
    }};

    private static boolean isManagerRunning = false;
    private static final Object sync = new Object();

    private static final GeneratorManager instance;
    private static UUID runningTaskUUID;

    static {
        instance = new GeneratorManager();
    }

    public static UUID queue(BuildGenerator generator, Consumer<Double> onUpdate, Consumer<GeneratorTask> onFinish, BiConsumer<TaskType, Integer> priorityChangeUpdate) {
        synchronized (sync) {
            GeneratorTask task = new GeneratorTask(generator, onUpdate, onFinish, priorityChangeUpdate);
            allTasks.get(TaskType.PRIMARY).add(task);
            if (!isManagerRunning) {
                isManagerRunning = true;
                new Thread(instance).start();
            }
            return task.uuid();
        }
    }

    public static int placeInLine(UUID taskUUID) {
        synchronized (sync) {
            for (List<GeneratorTask> tasks : allTasks.values()) {
                int place = 0;
                for (GeneratorTask task : tasks) {
                    if (task.equalsUUID(taskUUID))
                        return place + 1;
                    place++;
                }
            }
            return -1;
        }
    }

    public static void cancel(UUID taskUUID) {
        synchronized (sync) {
            for (List<GeneratorTask> tasks : allTasks.values()) {
                tasks.removeIf(task -> task.equalsUUID(taskUUID));
            }
            if (runningTaskUUID.equals(taskUUID))
                runningTaskUUID = null;
        }
    }

    @Override
    public void run() {
        while (true) {
            // get a task to work on
            GeneratorTask runningTask = null;
            TaskType taskType = null;
            synchronized (sync) {
                for (TaskType typeToCheck : TaskType.order()) {
                    if (!allTasks.get(typeToCheck).isEmpty()) {
                        runningTask = allTasks.get(typeToCheck).remove(0);
                        runningTaskUUID = runningTask.uuid();
                        taskType = typeToCheck;
                        break;
                    }
                }
                if (runningTask == null) {
                    isManagerRunning = false;
                    return;
                }
            }
            runningTask.waitForUpdate();

            synchronized (sync) {
                if (runningTaskUUID == null) {
                    // the running task was canceled
                    continue;
                }
            }
            synchronized (sync) {
                List<GeneratorTask> myTaskTypeTasks = allTasks.get(taskType);
                if (runningTask.isImpossible() || runningTask.isComplete()) {
                    // the task is impossible, or the task is done, finish the task, and don't add it to the queue
                    int i = 1;
                    for (GeneratorTask task : myTaskTypeTasks) {
                        task.priorityChange(TaskType.BACKGROUND, i++);
                    }
                    runningTask.onFinish();
                } else {
                    // the task is not impossible
                    // if the task is hard, add it to the background, otherwise add it to the corresponding task
                    if (taskType == TaskType.INDEXING || !runningTask.isHard()) {
                        // add it back to the indexing
                        runningTask.update();
                        myTaskTypeTasks.add(0, runningTask);
                    } else {
                        // if the task was a background task before, add it to the start of the queue
                        // otherwise add it to the end of the queue
                        if (taskType == TaskType.BACKGROUND) {
                            myTaskTypeTasks.add(0, runningTask);
                            runningTask.update();
                        } else {
                            myTaskTypeTasks.add(runningTask);
                            runningTask.priorityChange(TaskType.BACKGROUND, myTaskTypeTasks.size());
                        }
                    }
                }
            }
        }
    }

    public static final class GeneratorTask {
        private static final long DESIRED_MILLIS_TO_RUN = 15000;
        private static final long MAX_MILLIS_TO_RUN = 22000;
        private static final long UPDATE_INTERVAL = 4000;
        private final BuildGenerator generator;
        private final Consumer<Double> onUpdate;
        private final Consumer<GeneratorTask> onFinish;
        private final BiConsumer<TaskType, Integer> priorityChangeUpdate;
        private int impossibleCount = 0;
        private int hardTimeout = 0;
        private boolean isComplete = false;
        private final UUID uuid = UUID.randomUUID();
        private long lastProgressUpdate = 0;

        private GeneratorTask(BuildGenerator generator, Consumer<Double> onUpdate, Consumer<GeneratorTask> onFinish, BiConsumer<TaskType, Integer> priorityChangeUpdate) {
            this.generator = generator;
            this.onUpdate = onUpdate;
            this.onFinish = onFinish;
            this.priorityChangeUpdate = priorityChangeUpdate;
        }

        public void waitForUpdate() {
            BuildGenerator.ExitType exitType = this.generator.runFor(DESIRED_MILLIS_TO_RUN, MAX_MILLIS_TO_RUN, this::update);
            if (exitType == BuildGenerator.ExitType.IMPOSSIBLE) {
                impossibleCount++;
                if (isImpossible())
                    isComplete = true;
            } else {
                impossibleCount = 0;
                if (exitType == BuildGenerator.ExitType.HARD_TIMEOUT) {
                    hardTimeout++;
                }
                if (exitType == BuildGenerator.ExitType.COMPLETE) isComplete = true;
            }
        }

        public boolean isImpossible() {
            return impossibleCount >= 5;
        }

        public boolean isHard() {
            return hardTimeout >= 10;
        }

        public boolean isComplete() {
            return isComplete;
        }

        public void onFinish() {
            onFinish.accept(this);
        }

        public void update(double progress) {
            if (isComplete()) return;
            if (System.currentTimeMillis() - UPDATE_INTERVAL > this.lastProgressUpdate) {
                onUpdate.accept(progress);
                this.lastProgressUpdate = System.currentTimeMillis();
            }
        }

        public void update() {
            update(this.generator.progress());
        }

        public void priorityChange(TaskType taskType, int placeInQueue) {
            if (isComplete()) return;
            priorityChangeUpdate.accept(taskType, placeInQueue);
        }


        public UUID uuid() {
            return this.uuid;
        }

        public boolean equalsUUID(UUID uuid) {
            return this.uuid.equals(uuid);
        }
    }

    public enum TaskType {
        PRIMARY(0),
        BACKGROUND(1),
        INDEXING(2);

        private static TaskType[] listOrder;
        private final int order;

        TaskType(int order) {
            this.order = order;
        }

        public static TaskType[] order() {
            if (listOrder == null) {
                listOrder = new TaskType[values().length];
                for (TaskType taskType : values()) {
                    listOrder[taskType.order] = taskType;
                }
            }
            return listOrder;
        }
    }
}
