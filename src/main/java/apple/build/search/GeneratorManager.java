package apple.build.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class GeneratorManager implements Runnable {
    private static final List<GeneratorTask> primaryTasks = new ArrayList<>();
    private static final List<GeneratorTask> backgroundTasks = new ArrayList<>();

    private static GeneratorTask runningTask = null;
    private static boolean isManagerRunning = false;
    private static final Object isManagerRunningSync = new Object();

    private static final GeneratorManager instance;

    static {
        instance = new GeneratorManager();
    }

    public static void queue(BuildGenerator generator, Consumer<Double> onUpdate, Runnable onFinish) {
        synchronized (isManagerRunningSync) {
            primaryTasks.add(new GeneratorTask(generator, onUpdate, onFinish));
            if (!isManagerRunning) {
                isManagerRunning = true;
                new Thread(instance).start();
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            // get a task to work on
            synchronized (isManagerRunningSync) {
                if (primaryTasks.isEmpty()) {
                    if (backgroundTasks.isEmpty()) {
                        // be done running
                        isManagerRunning = false;
                        return;
                    } else {
                        // run a background task for a bit
                        runningTask = backgroundTasks.remove(0);
                    }
                } else {
                    // run a primary task
                    runningTask = primaryTasks.remove(0);
                }
            }
            runningTask.waitForUpdate();
        }
    }

    private static final class GeneratorTask {
        private final BuildGenerator generator;
        private final Consumer<Double> onUpdate;
        private final Runnable onFinish;
        private TaskType taskType = TaskType.PRIMARY;
        private boolean isFinished = false;

        private GeneratorTask(BuildGenerator generator, Consumer<Double> onUpdate, Runnable onFinish) {
            this.generator = generator;
            this.onUpdate = onUpdate;
            this.onFinish = onFinish;
        }

        public void waitForUpdate() {
            this.generator.generateLowerLevel();
        }
    }

    private enum TaskType {
        PRIMARY,
        BACKGROUND
    }
}
