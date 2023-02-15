package tech.illuin.indexed.operator.map.strategy;

import java.util.List;
import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface MapIndexStrategy<T>
{
    Function<T, ?> getQueryFunction();

    void push(List<T> values, T value);
}
