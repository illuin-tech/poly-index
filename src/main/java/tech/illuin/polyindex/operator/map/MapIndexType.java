package tech.illuin.polyindex.operator.map;

import tech.illuin.polyindex.operator.map.strategy.*;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface MapIndexType
{
    MapIndexType ALL = IndexAllStrategy::new;
    MapIndexType FIRST = IndexFirstStrategy::new;
    MapIndexType LAST = IndexLastStrategy::new;

    <T> MapIndexStrategy<T> provide(Function<T, ?> indexFunction);
}
