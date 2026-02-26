package tech.illuin.polyindex;

import tech.illuin.polyindex.exception.IndexClosingException;
import tech.illuin.polyindex.exception.UndefinedKeyException;
import tech.illuin.polyindex.key.Key;
import tech.illuin.polyindex.operator.IndexOperator;
import tech.illuin.polyindex.operator.lucene.LuceneOperator;
import tech.illuin.polyindex.operator.map.MapOperator;
import tech.illuin.polyindex.query.IndexKeyCollection;
import tech.illuin.polyindex.query.Query;

import java.util.*;
import java.util.stream.Stream;

/**
 * TODO: Migrate towards https://github.com/npgall/cqengine ?
 *
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MapStore<T> implements IndexedStore<T>
{
    private final Index<T> index;
    private final Map<IndexType, IndexOperator<T>> operators;

    /**
     *
     * @param index
     */
    public MapStore(Index<T> index)
    {
        logger.debug("Initializing map store with index registry containing {} key(s)", index.size());
        this.index = index;
        this.operators = Map.of(
            IndexType.MAP, new MapOperator<>(index, this::createMap),
            IndexType.LUCENE, new LuceneOperator<>(index, this::createMap)
        );
    }

    protected <V> Map<Key<T>, V> createMap()
    {
        return new HashMap<>();
    }

    @Override
    public MapStore<T> push(T value)
    {
        for (Key<T> indexKey : this.index.keys())
        {
            Object key = indexKey.computeIndexingKey(value);
            IndexOperator<T> operator = this.getOperator(indexKey.type());
            resolveKeyValue(key).forEach(k -> operator.push(indexKey, k, value));
        }
        return this;
    }

    @Override
    public boolean containsMatch(T match)
    {
        for (Key<T> indexKey : this.index.keys())
        {
            Object key = indexKey.computeQueryingKey(match);
            IndexOperator<T> operator = this.getOperator(indexKey.type());
            if (resolveKeyValue(key).anyMatch(k -> operator.contains(indexKey, k)))
                return true;
        }
        return false;
    }

    @Override
    public Set<Key<T>> keys()
    {
        return this.index.keys();
    }

    @Override
    public List<T> get(List<Query<T>> queries)
    {
        for (Query<T> query : queries)
        {
            Key<T> key = query.key();
            IndexOperator<T> operator = this.getOperator(key.type());
            Optional<List<T>> results = resolveKeyValue(query.value())
                .map(k -> operator.get(key, k))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

            if (results.map(l -> !l.isEmpty()).orElse(false))
                return results.get();
        }

        return Collections.emptyList();
    }
    
    @Override
    public List<T> getAll(Key<T> key)
    {
        return this.getOperator(key.type()).getAll(key).orElseGet(Collections::emptyList);
    }
    
    @Override
    public int count(Key<T> key)
    {
        return this.getOperator(key.type()).count(key);
    }
    
    @Override
    public List<T> remove(Query<T> query)
    {
        if (!this.index.keys().contains(query.key()))
            throw new UndefinedKeyException("The requested key has not been registered as part of this store's index.");

        Key<T> key = query.key();
        IndexOperator<T> operator = this.getOperator(key.type());
        return resolveKeyValue(query.value())
            .map(k -> operator.remove(key, k))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseGet(Collections::emptyList);
    }

    private IndexOperator<T> getOperator(IndexType family)
    {
        IndexOperator<T> operator = this.operators.get(family);
        if (operator == null)
            throw new IllegalArgumentException("The provided family " + family + " does not have a corresponding IndexOperator");
        return operator;
    }

    @Override
    public boolean isEmpty()
    {
        return this.operators.values().stream()
            .map(IndexOperator::isEmpty)
            .reduce((a, b) -> a && b)
            .orElse(true);
    }

    @Override
    public void close() throws IndexClosingException
    {
        for (IndexOperator<T> operator : this.operators.values())
            operator.close();
    }

    private static Stream<Object> resolveKeyValue(Object key)
    {
        return key instanceof IndexKeyCollection keyCollection
            ? keyCollection.stream()
            : Stream.of(key);
    }
}
