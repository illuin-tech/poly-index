package tech.illuin.indexed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.indexed.exception.IndexClosingException;
import tech.illuin.indexed.key.Key;
import tech.illuin.indexed.query.Query;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface IndexedStore<T> extends AutoCloseable
{
    Logger logger = LoggerFactory.getLogger(IndexedStore.class);

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

    default Optional<T> getFirst(Object match, Key<T> key)
    {
        return this.getFirst(Collections.singletonList(key.query(match)));
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

    Set<Key<T>> keys();

    List<T> get(List<Query<T>> queries);

    List<T> getAll(Key<T> key);

    default List<T> getMatch(T match, Key<T> key)
    {
        return this.get(key.match(match));
    }

    default List<T> getMatch(T match, List<Key<T>> keys)
    {
        return this.get(keys.stream().map(key -> key.match(match)).collect(Collectors.toList()));
    }

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

    @Override
    default void close() throws IndexClosingException {}
}
