package tech.illuin.indexed.operator.map.strategy;

import java.util.List;
import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IndexFirstStrategy<T> extends CompositeStrategy<T>
{
    public IndexFirstStrategy(Function<T, ?> queryFunction)
    {
        super(queryFunction);
    }

    @Override
    public void push(List<T> values, T value)
    {
        if (values.isEmpty())
            values.add(value);
    }
}
