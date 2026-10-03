package co.lettermint;

import co.lettermint.types.CursorPage;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Every item of a cursor-paginated list, returned by the {@code iterate()} methods.
 *
 * <pre>{@code
 * for (DomainListData domain : lettermint.domains().iterate()) { ... }
 * lettermint.messages().iterate(query).stream().limit(100).forEach(...);
 * }</pre>
 *
 * <p>Pages are requested lazily, when iteration gets to them, by following {@code next_cursor} until it
 * is null or empty, or repeats. Each {@link #iterator()} starts again from the first page. Errors are
 * thrown from {@code hasNext()} and {@code next()}.
 *
 * @param <T> the item type
 */
public final class CursorIterable<T> implements Iterable<T> {
    private final Function<String, CursorPage<T>> fetch;

    CursorIterable(Function<String, CursorPage<T>> fetch) {
        this.fetch = fetch;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Iterator<T> items = Collections.emptyIterator();
            private final Set<String> seen = new HashSet<>();
            private String cursor;
            private boolean started;
            private boolean done;

            @Override
            public boolean hasNext() {
                while (!items.hasNext() && !done) {
                    CursorPage<T> page = fetch.apply(started ? cursor : null);
                    started = true;
                    items = page.data().iterator();
                    String next = page.nextCursor();
                    if (next == null || next.isEmpty() || !seen.add(next)) {
                        done = true;
                    } else {
                        cursor = next;
                    }
                }
                return items.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return items.next();
            }
        };
    }

    /**
     * @return a sequential stream over every item; pages are requested as the stream consumes them
     */
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    @Override
    public String toString() {
        return "CursorIterable";
    }
}
