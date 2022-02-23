package tech.illuin.indexed;

import tech.illuin.indexed.exception.UndefinedKeyException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface IndexedStore<T>
{
    IndexedStore<T> push(T value);

    default IndexedStore<T> pushAll(Collection<T> values)
    {
        for (T value : values)
            this.push(value);
        return this;
    }

    default boolean contains(Object match, Key<T> key)
    {
        return this.contains(key.query(match));
    }

    default boolean contains(Object match, List<Key<T>> keys)
    {
        return this.contains(keys.stream().map(key -> key.query(match)).collect(Collectors.toList()));
    }

    default boolean contains(Query<T> query)
    {
        return !this.get(query).isEmpty();
    }

    default boolean contains(List<Query<T>> queries)
    {
        return !this.get(queries).isEmpty();
    }

    boolean containsMatch(T match);

    default boolean containsMatch(T match, Key<T> key)
    {
        return this.contains(key.match(match));
    }

    default boolean containsMatch(T match, List<Key<T>> keys)
    {
        return this.contains(keys.stream().map(key -> key.match(match)).collect(Collectors.toList()));
    }

    default Optional<T> getFirst(Object match, List<Key<T>> keys)
    {
        return this.getFirst(keys.stream().map(key -> key.query(match)).collect(Collectors.toList()));
    }

    default Optional<T> getFirst(Query<T> query)
    {
        return this.getFirst(Collections.singletonList(query));
    }

    default Optional<T> getFirst(List<Query<T>> queries)
    {
        List<T> items = this.get(queries);
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }

    default Optional<T> getFirstMatch(T match, Key<T> key)
    {
        return this.getFirst(key.match(match));
    }

    default Optional<T> getFirstMatch(T match, List<Key<T>> keys)
    {
        return this.getFirst(keys.stream().map(key -> key.match(match)).collect(Collectors.toList()));
    }

    default List<T> get(Object match, Key<T> key)
    {
        return this.get(key.query(match));
    }

    default List<T> get(Object match, List<Key<T>> keys)
    {
        return this.get(keys.stream().map(key -> key.query(match)).collect(Collectors.toList()));
    }

    default List<T> get(Query<T> query)
    {
        return this.get(Collections.singletonList(query));
    }

    List<T> get(List<Query<T>> queries);

    /**
     * Returns all entries under the provided index key.
     *
     * @param key
     * @return
     * @throws UndefinedKeyException
     */
    List<T> getAll(Key<T> key);

    default List<T> getMatch(T match, Key<T> key)
    {
        return this.get(key.match(match));
    }

    default List<T> getMatch(T match, List<Key<T>> keys)
    {
        return this.get(keys.stream().map(key -> key.match(match)).collect(Collectors.toList()));
    }

    /**
     * Counts all entries under the provided index key.
     *
     * @param key
     * @return
     * @throws UndefinedKeyException
     */
    int count(Key<T> key);

    default List<T> remove(Object match, Key<T> key)
    {
        return this.remove(key.query(match));
    }

    List<T> remove(Query<T> query);

    default List<T> removeMatch(T match, Key<T> key)
    {
        return this.remove(key.match(match));
    }

    boolean isEmpty();
}
