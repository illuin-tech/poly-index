package tech.illuin.indexed.operator.map;

import tech.illuin.indexed.IndexType;
import tech.illuin.indexed.key.BasicKey;
import tech.illuin.indexed.operator.map.strategy.IndexAllStrategy;
import tech.illuin.indexed.operator.map.strategy.MapIndexStrategy;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MapKey<T> extends BasicKey<T>
{
    private final MapIndexStrategy<T> strategy;

    public MapKey(Function<T, ?> function)
    {
        this(function, new IndexAllStrategy<>(function));
    }

    public MapKey(Function<T, ?> function, MapIndexStrategy<T> strategy)
    {
        this(null, function, strategy);
    }

    public MapKey(String name, Function<T, ?> function, MapIndexStrategy<T> strategy)
    {
        super(name, function, IndexType.MAP);
        this.strategy = strategy;
    }

    @Override
    public Object computeQueryingKey(T value)
    {
        return this.strategy().getQueryFunction().apply(value);
    }

    public MapIndexStrategy<T> strategy()
    {
        return this.strategy;
    }
}
