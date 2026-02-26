package tech.illuin.polyindex.operator;

import tech.illuin.polyindex.IndexType;
import tech.illuin.polyindex.key.Key;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface ReferenceProvider
{
    <T> Key<T> provide(Function<T, ?> function, IndexType type);
}
