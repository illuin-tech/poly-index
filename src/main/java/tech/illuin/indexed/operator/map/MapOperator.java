package tech.illuin.indexed.operator.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.indexed.Index;
import tech.illuin.indexed.IndexingType;
import tech.illuin.indexed.exception.UndefinedKeyException;
import tech.illuin.indexed.key.Key;
import tech.illuin.indexed.operator.IndexFamily;
import tech.illuin.indexed.operator.IndexOperator;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MapOperator<T> implements IndexOperator<T>
{
    private final Map<Key<T>, ValueMap<T>> maps;

    private static final Logger logger = LoggerFactory.getLogger(MapOperator.class);

    public MapOperator(Index<T> index, Supplier<Map<?, ?>> map)
    {
        //noinspection unchecked
        this.maps = (Map<Key<T>, ValueMap<T>>) map.get();
        for (Key<T> key : index.keys())
        {
            if (key.family() != IndexFamily.MAP)
                continue;
            this.maps.put(key, new ValueMap<>());
        }
        if (!this.maps.isEmpty())
            logger.trace("Initialized map operator for index registry with {} MAP key(s)", this.maps.size());
    }

    @Override
    public void push(Key<T> indexKey, Object key, T value)
    {
        if (!this.maps.get(indexKey).containsKey(key))
            this.maps.get(indexKey).put(key, new ArrayList<>());

        List<T> values = this.maps.get(indexKey).get(key);

        /* If we need to index all submitted values OR it has not been initialized yet */
        /* Note that this already takes care of IndexingType.FIRST by initializing the data but never updating it */
        if (indexKey.type() == IndexingType.ALL || values.isEmpty())
        {
            logger.trace("Pushing value for key \"{}\" to index {} of type {}", key, indexKey.name(), indexKey.type());
            values.add(value);
        }
        else if (indexKey.type() == IndexingType.LAST)
        {
            logger.trace("Pushing value for key \"{}\" to index {} of type {}", key, indexKey.name(), indexKey.type());
            values.set(0, value);
        }
    }

    @Override
    public boolean containsMatch(Key<T> indexKey, Object key)
    {
        return this.maps.get(indexKey).containsKey(key);
    }

    @Override
    public Optional<List<T>> get(Key<T> indexKey, Object key)
    {
        if (!this.maps.containsKey(indexKey))
            return Optional.empty();
        if (key == null || !this.maps.get(indexKey).containsKey(key))
            return Optional.empty();

        return Optional.of(this.maps.get(indexKey).get(key));
    }

    @Override
    public Optional<List<T>> getAll(Key<T> indexKey)
    {
        if (!this.maps.containsKey(indexKey))
            throw new UndefinedKeyException("The provided key is not part of this store's index.");

        List<T> result = new ArrayList<>();
        for (List<T> values : this.maps.get(indexKey).values())
            result.addAll(values);

        return Optional.of(result);
    }

    @Override
    public int count(Key<T> indexKey)
    {
        if (!this.maps.containsKey(indexKey))
            throw new UndefinedKeyException("The provided key is not part of this store's index.");

        return this.maps.get(indexKey).values().stream()
            .map(Collection::size)
            .reduce(0, Integer::sum)
        ;
    }

    @Override
    public Optional<List<T>> remove(Key<T> indexKey, Object key)
    {
        if (!this.maps.containsKey(indexKey))
            return Optional.empty();
        if (key == null || !this.maps.get(indexKey).containsKey(key))
            return Optional.empty();

        return Optional.of(this.maps.get(indexKey).remove(key));
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
