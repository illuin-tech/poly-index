package tech.illuin.indexed.operator.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.indexed.Index;
import tech.illuin.indexed.IndexType;
import tech.illuin.indexed.exception.UndefinedKeyException;
import tech.illuin.indexed.key.Key;
import tech.illuin.indexed.operator.IndexOperator;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MapOperator<T> implements IndexOperator<T>
{
    private final Map<MapKey<T>, ValueMap<T>> maps;

    private static final Logger logger = LoggerFactory.getLogger(MapOperator.class);

    public MapOperator(Index<T> index, Supplier<Map<?, ?>> map)
    {
        //noinspection unchecked
        this.maps = (Map<MapKey<T>, ValueMap<T>>) map.get();
        for (Key<T> key : index.keys())
        {
            if (key.type() != IndexType.MAP)
                continue;
            this.maps.put((MapKey<T>) key, new ValueMap<>());
        }
        if (!this.maps.isEmpty())
            logger.trace("Initialized map operator for index registry with {} MAP key(s)", this.maps.size());
    }

    @Override
    public void push(Key<T> indexKey, Object key, T value)
    {
        MapKey<T> mapKey = this.validateKey(indexKey, true);

        if (key == null)
            return;
        if (!this.maps.get(mapKey).containsKey(key))
            this.maps.get(mapKey).put(key, new ArrayList<>());

        List<T> values = this.maps.get(mapKey).get(key);

        logger.trace("Submitting value for key \"{}\" to index {} of type {}", key, mapKey.name(), mapKey.type());
        mapKey.strategy().push(values, value);
    }

    @Override
    public boolean contains(Key<T> indexKey, Object key)
    {
        MapKey<T> mapKey = this.validateKey(indexKey);

        return this.maps.get(mapKey).containsKey(key);
    }

    @Override
    public Optional<List<T>> get(Key<T> indexKey, Object key)
    {
        MapKey<T> mapKey = this.validateKey(indexKey);

        if (!this.maps.containsKey(mapKey))
            return Optional.empty();
        if (key == null || !this.maps.get(mapKey).containsKey(key))
            return Optional.empty();

        return Optional.of(this.maps.get(mapKey).get(key));
    }

    @Override
    public Optional<List<T>> getAll(Key<T> indexKey)
    {
        MapKey<T> mapKey = this.validateKey(indexKey, true);

        List<T> result = new ArrayList<>();
        for (List<T> values : this.maps.get(mapKey).values())
            result.addAll(values);

        return Optional.of(result);
    }

    @Override
    public int count(Key<T> indexKey)
    {
        MapKey<T> mapKey = this.validateKey(indexKey, true);

        return this.maps.get(mapKey).values().stream()
            .map(Collection::size)
            .reduce(0, Integer::sum)
        ;
    }

    @Override
    public Optional<List<T>> remove(Key<T> indexKey, Object key)
    {
        MapKey<T> mapKey = this.validateKey(indexKey);

        if (!this.maps.containsKey(mapKey))
            return Optional.empty();
        if (key == null || !this.maps.get(mapKey).containsKey(key))
            return Optional.empty();

        return Optional.of(this.maps.get(mapKey).remove(key));
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

    private MapKey<T> validateKey(Key<T> key)
    {
        return this.validateKey(key, false);
    }

    private MapKey<T> validateKey(Key<T> key, boolean checkRegistration)
    {
        if (!(key instanceof MapKey<T> mapKey))
            throw new IllegalArgumentException("An unexpected index key of type " + key.getClass() + " was supplied while a MapKey is expected");
        if (checkRegistration && !this.maps.containsKey(key))
            throw new UndefinedKeyException("The provided key is not part of this store's indexes");
        return mapKey;
    }

    protected static class ValueMap<T> extends HashMap<Object, List<T>> {}
}
