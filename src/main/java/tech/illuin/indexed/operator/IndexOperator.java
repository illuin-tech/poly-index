package tech.illuin.indexed.operator;

import tech.illuin.indexed.exception.IndexClosingException;
import tech.illuin.indexed.key.Key;

import java.util.List;
import java.util.Optional;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface IndexOperator<T> extends AutoCloseable
{
    void push(Key<T> indexKey, Object key, T value);

    boolean contains(Key<T> indexKey, Object key);

    Optional<List<T>> get(Key<T> indexKey, Object key);

    Optional<List<T>> getAll(Key<T> indexKey);

    int count(Key<T> indexKey);

    Optional<List<T>> remove(Key<T> indexKey, Object key);

    boolean isEmpty();

    @Override
    default void close() throws IndexClosingException {}
}
