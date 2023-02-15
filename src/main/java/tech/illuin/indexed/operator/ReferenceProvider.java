package tech.illuin.indexed.operator;

import tech.illuin.indexed.IndexType;
import tech.illuin.indexed.key.Key;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface ReferenceProvider
{
    <T> Key<T> provide(Function<T, ?> function, IndexType type);
}
