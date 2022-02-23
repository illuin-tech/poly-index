package tech.illuin.indexed;

import tech.illuin.indexed.exception.UndefinedKeyException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO: Migrate towards https://github.com/npgall/cqengine ?
 *
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MapStore<T> implements IndexedStore<T>
{
    private final Index<T> index;
    private final Map<Key<T>, ValueMap<T>> maps;

    /**
     *
     * @param index
     */
    public MapStore(Index<T> index)
    {
        this.index = index;
        this.maps = this.createMap();
        for (Key<T> key : index.keys())
            this.maps.put(key, new ValueMap<>());
    }

    protected Map<Key<T>, ValueMap<T>> createMap()
    {
        return new HashMap<>();
    }

    @Override
    public MapStore<T> push(T value)
    {
        for (Key<T> indexKey : this.index.keys())
        {
            Object key = indexKey.compute(value);
            if (key == null)
                continue;

            if (!this.maps.get(indexKey).containsKey(key))
                this.maps.get(indexKey).put(key, new ArrayList<>());

            List<T> values = this.maps.get(indexKey).get(key);

            /* If we need to index all submitted values OR it has not been initialized yet */
            /* Note that this already takes care of IndexingType.FIRST by initializing the data but never updating it */
            if (indexKey.type() == IndexingType.ALL || values.isEmpty())
                values.add(value);
            else if (indexKey.type() == IndexingType.LAST)
                values.set(0, value);
        }
        return this;
    }

    @Override
    public boolean containsMatch(T match)
    {
        for (Key<T> indexKey : this.index.keys())
        {
            Object key = indexKey.compute(match);
            if (key == null)
                continue;

            if (this.maps.get(indexKey).containsKey(key))
                return true;
        }
        return false;
    }
    
    @Override
    public List<T> get(List<Query<T>> queries)
    {
        for (Query<T> query : queries)
        {
            Key<T> key = query.key();
            Object value = query.value();
            if (!this.maps.containsKey(key))
                continue;
            if (value == null || !this.maps.get(key).containsKey(value))
                continue;

            return this.maps.get(key).get(value);
        }

        return new ArrayList<>();
    }
    
    @Override
    public List<T> getAll(Key<T> key)
    {
        if (!this.maps.containsKey(key))
            throw new UndefinedKeyException("The provided key is not part of this store's index.");

        return this.maps.get(key).values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList())
        ;
    }
    
    @Override
    public int count(Key<T> key)
    {
        if (!this.maps.containsKey(key))
            throw new UndefinedKeyException("The provided key is not part of this store's index.");

        return this.maps.get(key).values().stream()
            .map(Collection::size)
            .reduce(Integer::sum)
            .orElse(0)
        ;
    }
    
    @Override
    public List<T> remove(Query<T> query)
    {
        Key<T> key = query.key();
        Object value = query.value();

        if (!this.index.keys().contains(query.key()))
            throw new UndefinedKeyException("The requested key has not been registered as part of this store's index.");

        if (!this.maps.containsKey(key))
            return Collections.emptyList();
        if (value == null || !this.maps.get(key).containsKey(value))
            return Collections.emptyList();

        return this.maps.get(key).remove(value);
    }

    @Override
    public boolean isEmpty()
    {
        for (ValueMap<T> vals : this.maps.values())
        {
            if (!vals.isEmpty())
                return false;
        }
        return true;
    }

    protected static class ValueMap<T> extends HashMap<Object, List<T>> {}
}
