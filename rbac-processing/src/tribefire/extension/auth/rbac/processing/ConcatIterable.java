package tribefire.extension.auth.rbac.processing;
import java.util.*;

/**
 * Concatenates multiple Iterables into a single Iterable (lazy).
 */
public final class ConcatIterable<T> implements Iterable<T> {

    private final List<? extends Iterable<? extends T>> parts;

    @SafeVarargs
    public ConcatIterable(Iterable<? extends T>... parts) {
        this(Arrays.asList(Objects.requireNonNull(parts, "parts")));
    }

    public ConcatIterable(List<? extends Iterable<? extends T>> parts) {
        this.parts = Objects.requireNonNull(parts, "parts");
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {

            private int partIndex = 0;
            private Iterator<? extends T> current = Collections.emptyIterator();
            private Iterator<? extends T> lastReturnedFrom = null;

            @Override
            public boolean hasNext() {
                while (true) {
                    if (current != null && current.hasNext()) {
                        return true;
                    }
                    if (partIndex >= parts.size()) {
                        return false;
                    }
                    Iterable<? extends T> nextPart = parts.get(partIndex++);
                    current = (nextPart == null) ? Collections.emptyIterator() : nextPart.iterator();
                }
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                lastReturnedFrom = current;
                return current.next();
            }

            @Override
            public void remove() {
                if (lastReturnedFrom == null) throw new IllegalStateException("next() not called yet");
                // Delegate to the underlying iterator if it supports remove().
                lastReturnedFrom.remove();
                lastReturnedFrom = null;
            }
        };
    }

    public static <T> Iterable<T> of(List<? extends Iterable<? extends T>> parts) {
        return new ConcatIterable<>(parts);
    }

    @SafeVarargs
    public static <T> Iterable<T> of(Iterable<? extends T>... parts) {
        return new ConcatIterable<>(parts);
    }
}
