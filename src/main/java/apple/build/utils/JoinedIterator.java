package apple.build.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class JoinedIterator<T> implements Iterator<T> {
    private final Iterator<T>[] iterators;
    private int index = 0;
    private Iterator<T> currentIterator = null;

    public JoinedIterator(Iterator<T>[] iterators) {
        this.iterators = iterators;
    }

    @Override
    public boolean hasNext() {
        if (currentIterator == null) {
            if (index == iterators.length) return false;
            currentIterator = iterators[index++];
        }
        while (!currentIterator.hasNext()) {
            if (index == iterators.length) return false;
            currentIterator = iterators[index++];
        }
        return true;
    }

    @Override
    public T next() {
        if (!currentIterator.hasNext()) {
            throw new NoSuchElementException("There are no more iterators");
        }
        return currentIterator.next();
    }

    @Override
    public void remove() {
        currentIterator.remove();
    }
}
