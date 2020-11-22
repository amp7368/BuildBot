package apple.build.data.threads;

import apple.build.data.BuildGenerator;
import apple.build.utils.Pair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class GeneratorForkJoinPool {
    public static final int THOUSANDS = 10000;

    private final AtomicInteger sizeLeftInThousands = new AtomicInteger(THOUSANDS);
    private final BiConsumer<BuildGenerator, Float> todo;
    private final ForkJoinPool pool;
    private final Queue<ForkJoinTask<?>> tasksToJoin = new ConcurrentLinkedDeque<>();
    private final Queue<Pair<BuildGenerator, Float>> generatorsToAdd;
    private final int myThreadsSize;
    private final float threadsToUse;
    private final int layer;
    public GeneratorForkJoinPool(List<BuildGenerator> subGeneratorsRaw, BiConsumer<BuildGenerator, Float> todo, int myThreadsSize, float threadsToUse,int layer) {
        int generatorSize = subGeneratorsRaw.size();
        this.layer = layer;
        List<Pair<BuildGenerator, BigInteger>> subGenerators = new ArrayList<>(generatorSize);
        for (BuildGenerator generator : subGeneratorsRaw)
            subGenerators.add(new Pair<>(generator, generator.size()));
        BigInteger totalSizeInteger = BigInteger.ZERO;
        for (Pair<BuildGenerator, BigInteger> subGenerator : subGenerators) {
            totalSizeInteger = totalSizeInteger.add(subGenerator.getValue());
        }
        BigDecimal totalSizeDecimal = new BigDecimal(totalSizeInteger);
        List<Pair<BuildGenerator, Float>> subGeneratorsPercs = new ArrayList<>(generatorSize);
        for (Pair<BuildGenerator, BigInteger> subGenerator : subGenerators)
            subGeneratorsPercs.add(new Pair<>(subGenerator.getKey(), new BigDecimal(subGenerator.getValue()).divide(totalSizeDecimal, 4, RoundingMode.DOWN).floatValue() * THOUSANDS));
        subGeneratorsPercs.sort((b1, b2) -> b2.getValue().compareTo(b1.getValue()));

        this.generatorsToAdd = new ConcurrentLinkedDeque<>(subGeneratorsPercs);
        this.pool = new ForkJoinPool(myThreadsSize);
        this.todo = todo;
        this.myThreadsSize = myThreadsSize;
        this.threadsToUse = threadsToUse;
        int myRealThreadSize = Math.min(generatorSize, myThreadsSize);
        float myThreadsToGive = threadsToUse - myRealThreadSize;
        for (int i = -1; i < myThreadsSize && !generatorsToAdd.isEmpty(); i++) {
            Pair<BuildGenerator, Float> addMe = generatorsToAdd.remove();
            float leftToDoMultiplier = ((float) generatorSize) / myRealThreadSize;
            float threadToGiveThis = Math.max(1, myThreadsToGive * addMe.getValue() / sizeLeftInThousands.get() * leftToDoMultiplier);
            tasksToJoin.add(pool.submit(
                    new SubGeneratorRunnable(addMe.getKey(), threadToGiveThis, todo)
            ));
            this.sizeLeftInThousands.getAndUpdate(old -> Math.max(1, old - addMe.getValue().intValue()));
        }
    }

    public void waitForCompletion() {
        while (!generatorsToAdd.isEmpty() || !tasksToJoin.isEmpty()) {
            ForkJoinTask<?> join;
            while ((join = tasksToJoin.poll()) != null) {
                if (layer ==0) {
                    int a = 3;
                }
                join.join(); // if this throws an error, reduce the threads to 1 to find where the actual error is. it's not here
            }
            synchronized (this) {
            }
        }
    }

    private void finished() {
        synchronized (this) {
            Pair<BuildGenerator, Float> addMe = generatorsToAdd.poll();
            if (addMe == null) {
                return;
            }
            int generatorSize = generatorsToAdd.size();
            int myRealThreadSize = Math.min(generatorSize + 1, myThreadsSize);
            float myThreadsToGive = threadsToUse - myRealThreadSize;
            float leftToDoMultiplier = ((float) generatorSize) / myRealThreadSize;
            float threadToGiveThis = Math.max(1, myThreadsToGive * addMe.getValue() / sizeLeftInThousands.get() * leftToDoMultiplier);
            tasksToJoin.add(pool.submit(new SubGeneratorRunnable(addMe.getKey(), threadToGiveThis, todo)));
            this.sizeLeftInThousands.getAndUpdate(old -> Math.max(1, old - addMe.getValue().intValue()));
        }
    }

    private class SubGeneratorRunnable implements Runnable {
        private final BuildGenerator subGenerator;
        private final float threads;
        private final BiConsumer<BuildGenerator, Float> todo;

        public SubGeneratorRunnable(BuildGenerator subGenerator, float threads, BiConsumer<BuildGenerator, Float> todo) {
            this.subGenerator = subGenerator;
            this.threads = threads;
            this.todo = todo;
        }

        @Override
        public void run() {
            todo.accept(subGenerator, threads);
            finished();

        }
    }
}
