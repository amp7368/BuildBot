package apple.build.search;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GeneratorManager implements Runnable {
    private static final List<GeneratorTask> primaryTasks = new ArrayList<>();
    private static final List<GeneratorTask> backgroundTasks = new ArrayList<>();

    private static boolean isManagerRunning = false;
    private static final Object sync = new Object();

    private static final GeneratorManager instance;
    private static UUID runningTaskUUID;

    static {
        instance = new GeneratorManager();
    }

    public static UUID  queue(BuildGenerator generator, Consumer<Double> onUpdate, Consumer<GeneratorTask> onFinish, BiConsumer<TaskType, Integer> priorityChangeUpdate) {
        synchronized (sync) {
            GeneratorTask task = new GeneratorTask(generator, onUpdate, onFinish, priorityChangeUpdate);
            primaryTasks.add(task);
            if (!isManagerRunning) {
                isManagerRunning = true;
                new Thread(instance).start();
            }
            return task.uuid();
        }
    }

    public static int placeInLine(UUID taskUUID) {
        synchronized (sync) {
            int size = primaryTasks.size();
            for (int i = 0; i < size; i++) {
                GeneratorTask task = primaryTasks.get(i);
                if (task.equalsUUID(taskUUID))
                    return i + 1;
            }
            size = backgroundTasks.size();
            for (int i = 0; i < size; i++) {
                GeneratorTask task = backgroundTasks.get(i);
                if (task.equalsUUID(taskUUID))
                    return i + 1;
            }
            return -1;
        }
    }

    public static void cancel(UUID taskUUID) {
        synchronized (sync) {
            primaryTasks.removeIf(task -> task.equalsUUID(taskUUID));
            backgroundTasks.removeIf(task -> task.equalsUUID(taskUUID));
            if (runningTaskUUID.equals(taskUUID))
                runningTaskUUID = null;
        }
    }

    @Override
    public void run() {
        while (true) {
            // get a task to work on
            GeneratorTask runningTask;
            boolean wasBackground = false;
            synchronized (sync) {
                if (primaryTasks.isEmpty()) {
                    if (backgroundTasks.isEmpty()) {
                        // be done running
                        isManagerRunning = false;
                        return;
                    } else {
                        // run a background task for a bit
                        runningTask = backgroundTasks.remove(0);
                        wasBackground = true;
                    }
                } else {
                    // run a primary task
                    runningTask = primaryTasks.remove(0);
                }
                runningTaskUUID = runningTask.uuid();
            }
            runningTask.waitForUpdate();

            synchronized (sync) {
                if (runningTaskUUID == null) {
                    // the running task was canceled
                    System.out.println("the running task was canceled");
                    continue;
                }
            }
            synchronized (sync) {
                if (runningTask.isImpossible() || runningTask.isComplete()) {
                    // the task is impossible, or the task is done, finish the task, and don't add it to the queue
                    int i = 1;
                    if (wasBackground) {
                        for (GeneratorTask task : backgroundTasks) {
                            task.priorityChange(TaskType.BACKGROUND, i++);
                        }
                    } else {
                        for (GeneratorTask task : primaryTasks) {
                            task.priorityChange(TaskType.PRIMARY, i++);
                        }
                    }
                    runningTask.onFinish();
                } else {
                    // the task is not impossible

                    // if the task is hard, add it to the background, otherwise add it to the primary tasks
                    if (runningTask.isHard()) {
                        // if the task was a background task before, add it to the start of the queue
                        // otherwise add it to the end of the queue
                        if (wasBackground) {
                            backgroundTasks.add(0, runningTask);
                            runningTask.update();
                        } else {
                            backgroundTasks.add(runningTask);
                            runningTask.priorityChange(TaskType.BACKGROUND, backgroundTasks.size());
                        }
                    } else if (runningTask.isWorking()) {
                        runningTask.update();
                        primaryTasks.add(0, runningTask);
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

        public boolean isWorking() {
            return generator.isWorking();
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
        PRIMARY,
        BACKGROUND
    }
}
