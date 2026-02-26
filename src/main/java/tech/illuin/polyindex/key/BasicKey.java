package tech.illuin.polyindex.key;

import tech.illuin.polyindex.IndexType;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class BasicKey<T> implements Key<T>
{
    private final Function<T, ?> function;
    private final IndexType type;
    private final String name;

    public BasicKey(Function<T, ?> function, IndexType type)
    {
        this(null, function, type);
    }

    public BasicKey(String name, Function<T, ?> function, IndexType type)
    {
        this.function = function;
        this.type = type;
        this.name = name;
    }

    @Override
    public Object computeIndexingKey(T value)
    {
        return this.function.apply(value);
    }

    @Override
    public IndexType type()
    {
        return this.type;
    }

    @Override
    public String name()
    {
        return this.name != null ? this.name : Objects.toString(this);
    }
}
