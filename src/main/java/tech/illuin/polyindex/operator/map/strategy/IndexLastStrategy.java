package tech.illuin.polyindex.operator.map.strategy;

import java.util.List;
import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IndexLastStrategy<T> extends CompositeStrategy<T>
{
    public IndexLastStrategy(Function<T, ?> queryFunction)
    {
        super(queryFunction);
    }

    @Override
    public void push(List<T> values, T value)
    {
        if (values.isEmpty())
            values.add(value);
        else
            values.set(0, value);
    }
}
