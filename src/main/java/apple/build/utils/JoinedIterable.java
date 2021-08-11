package apple.build.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public record JoinedIterable<T>(Iterable<T>... iterables) implements Iterable<T> {
    @SafeVarargs
    public JoinedIterable {
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        Iterator<T>[] iterators = new Iterator[iterables.length];
        for (int i = 0; i < iterables.length; i++) {
            iterators[i] = iterables[i].iterator();
        }
        return new JoinedIterator<T>(iterators);
    }
}
