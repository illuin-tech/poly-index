package tech.illuin.polyindex.operator.map.strategy;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public abstract class CompositeStrategy<T> implements MapIndexStrategy<T>
{
    private final Function<T, ?> queryFunction;

    public CompositeStrategy(Function<T, ?> queryFunction)
    {
        this.queryFunction = queryFunction;
    }

    @Override
    public Function<T, ?> getQueryFunction()
    {
        return this.queryFunction;
    }
}
