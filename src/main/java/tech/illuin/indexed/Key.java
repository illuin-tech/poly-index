package tech.illuin.indexed;


import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class Key<T>
{
    private final Function<T, ?> function;
    private final IndexingType type;

    Key(Function<T, ?> function, IndexingType type)
    {
        this.function = function;
        this.type = type;
    }

    public static <T> Key<T> of(Function<T, ?> function)
    {
        return new Key<>(function, IndexingType.ALL);
    }

    public static <T> Key<T> of(Function<T, ?> function, IndexingType type)
    {
        return new Key<>(function, type);
    }

    public Object compute(T value)
    {
        return this.function.apply(value);
    }

    public Query<T> match(T value)
    {
        return new Query<>(this, this.function.apply(value));
    }

    public Query<T> query(Object value)
    {
        return new Query<>(this, value);
    }

    public IndexingType type()
    {
        return this.type;
    }
}
