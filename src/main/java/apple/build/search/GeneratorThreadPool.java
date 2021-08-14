package apple.build.search;

import apple.build.search.BuildGenerator;
import apple.build.utils.Pair;
import apple.build.utils.SystemUsage;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class GeneratorThreadPool {
    private final Consumer<BuildGenerator> toRun;
    private AtomicLong maxTimeToStop;
    private final PoolThread[] pool;

    private final List<Pair<BuildGenerator, BigInteger>> generatorsToStartWork;

    private BigInteger sizeBeingWorkedOn = BigInteger.ZERO;
    private final Object syncSize = new Object();
    private BuildGenerator.ExitType exitType = BuildGenerator.ExitType.COMPLETE;

    /**
     * @param subGeneratorsRaw the subGenerators for the parent generator
     * @param toRun            what the parent generator wants to be completed for each subGenerator
     * @param myThreadsSize    the number of threads for this generator
     * @param maxTimeToStop    when we should stop working on things
     */
    public GeneratorThreadPool(List<BuildGenerator> subGeneratorsRaw, Consumer<BuildGenerator> toRun, int myThreadsSize, AtomicLong maxTimeToStop) {
        this.toRun = toRun;
        this.maxTimeToStop = maxTimeToStop;
        int generatorSize = subGeneratorsRaw.size();
        List<Pair<BuildGenerator, BigInteger>> subGenerators = new ArrayList<>(generatorSize);
        for (BuildGenerator generator : subGeneratorsRaw)
            subGenerators.add(new Pair<>(generator, generator.size()));
        subGenerators.sort((b1, b2) -> b2.getValue().compareTo(b1.getValue()));
        this.generatorsToStartWork = subGenerators;
        this.pool = new PoolThread[Math.min(5, myThreadsSize)];
        for (int i = 0; i < this.pool.length; i++) {
            PoolThread poolThread = new PoolThread(i);
            this.pool[i] = poolThread;
            if (!this.generatorsToStartWork.isEmpty()) {
                Pair<BuildGenerator, BigInteger> generator = this.generatorsToStartWork.remove(0);
                poolThread.setGenerator(generator.getKey(), generator.getValue());
            }
        }
        for (PoolThread poolThread : pool) {
            poolThread.start();
        }
    }

    /**
     * @return true if the generator stopped prematurely
     */
    public BuildGenerator.ExitType waitForCompletion() {
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitType;
    }

    private synchronized void finished(int id) {
        this.pool[id] = null;
        boolean isReallyDone = true;
        for (PoolThread thread : this.pool) {
            if (thread != null) {
                isReallyDone = false;
                break;
            }
        }
        if (isReallyDone) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    private class PoolThread extends Thread {
        private final int id;
        private BuildGenerator generator;
        private BigInteger size = BigInteger.ZERO;

        private PoolThread(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {
                if (this.generator == null) {
                    Pair<BuildGenerator, BigInteger> generatorToWorkOn;
                    synchronized (generatorsToStartWork) {
                        if (!generatorsToStartWork.isEmpty()) {
                            generatorToWorkOn = generatorsToStartWork.remove(0);
                        } else {
                            finished(this.id);
                            return;
                        }
                    }
                    setGenerator(generatorToWorkOn.getKey(), generatorToWorkOn.getValue());
                }
                this.generator.generateLowerLevel();
                if (shouldStop(maxTimeToStop)) {
                    synchronized (syncSize) {
                        exitType = BuildGenerator.ExitType.HARD_TIMEOUT;
                    }
                    this.generator = null;
                    finished(this.id);
                    return;
                }
                this.generator = null;
            }
        }

        public void setGenerator(BuildGenerator generator, BigInteger size) {
            synchronized (syncSize) {
                this.generator = generator;
                sizeBeingWorkedOn = sizeBeingWorkedOn.subtract(this.size).add(size);
                this.size = size;
            }
        }
    }

    public static boolean shouldStop(AtomicLong maxTimeToStop) {
        return System.currentTimeMillis() > maxTimeToStop.get();
    }

    public static int getThreadsAvailable() {
        return (int) SystemUsage.getProcessorsFree() + 1;
    }
}
