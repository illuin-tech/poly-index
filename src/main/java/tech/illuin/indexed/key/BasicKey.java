package tech.illuin.indexed.key;

import tech.illuin.indexed.IndexingType;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class BasicKey<T> implements Key<T>
{
    private final Function<T, ?> function;
    private final IndexingType type;
    private final String name;

    public BasicKey(Function<T, ?> function, IndexingType type)
    {
        this(null, function, type);
    }

    public BasicKey(String name, Function<T, ?> function, IndexingType type)
    {
        this.function = function;
        this.type = type;
        this.name = name;
    }

    @Override
    public Object compute(T value)
    {
        return this.function.apply(value);
    }

    @Override
    public IndexingType type()
    {
        return this.type;
    }

    @Override
    public String name()
    {
        return this.name != null ? this.name : Objects.toString(this);
    }
}
