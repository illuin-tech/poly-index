package tech.illuin.indexed.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public final class IndexKeyCollection
{
    private final List<Object> keys;

    private IndexKeyCollection(Collection<?> keys)
    {
        this.keys = new ArrayList<>(keys.size());
        this.keys.addAll(keys);
    }

    public static IndexKeyCollection of(Collection<?> keys)
    {
        return new IndexKeyCollection(keys);
    }

    public Stream<Object> stream()
    {
        return this.keys.stream();
    }
}
