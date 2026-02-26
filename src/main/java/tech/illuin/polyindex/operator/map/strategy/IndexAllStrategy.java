package tech.illuin.polyindex.operator.map.strategy;

import java.util.List;
import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IndexAllStrategy<T> extends CompositeStrategy<T>
{
    public IndexAllStrategy(Function<T, ?> queryFunction)
    {
        super(queryFunction);
    }

    @Override
    public void push(List<T> values, T value)
    {
        values.add(value);
    }
}
