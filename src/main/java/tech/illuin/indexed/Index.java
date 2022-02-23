package tech.illuin.indexed;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class Index<T>
{
    private final Set<Key<T>> keys;

    private Index(Collection<Key<T>> keys)
    {
        this.keys = new HashSet<>(keys);
    }

    @SafeVarargs
    public static <T> Index<T> of(Key<T>... keys)
    {
        Set<Key<T>> keySet = Arrays.stream(keys).collect(Collectors.toSet());
        return new Index<>(keySet);
    }

    public Set<Key<T>> keys()
    {
        return this.keys;
    }

    public int size()
    {
        return this.keys.size();
    }
}
