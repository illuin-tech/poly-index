package tech.illuin.indexed.key;

import tech.illuin.indexed.IndexType;
import tech.illuin.indexed.operator.lucene.LuceneKey;
import tech.illuin.indexed.operator.lucene.strategy.LuceneIndexStrategy;
import tech.illuin.indexed.operator.lucene.strategy.PartialMatchStrategy;
import tech.illuin.indexed.operator.map.MapIndexType;
import tech.illuin.indexed.operator.map.MapKey;
import tech.illuin.indexed.operator.map.strategy.MapIndexStrategy;
import tech.illuin.indexed.query.Query;

import java.util.function.Function;

import static tech.illuin.indexed.operator.map.MapIndexType.ALL;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Key<T>
{
    Object computeIndexingKey(T value);

    default Object computeQueryingKey(T value)
    {
        return this.computeIndexingKey(value);
    }

    IndexType type();

    String name();

    default Query<T> match(T value)
    {
        return new Query<>(this, this.computeQueryingKey(value));
    }

    default Query<T> query(Object value)
    {
        return new Query<>(this, value);
    }

    static <T> Key<T> of(Function<T, ?> function)
    {
        return new MapKey<>(function, ALL.provide(function));
    }

    static <T> Key<T> of(String name, Function<T, ?> function)
    {
        return new MapKey<>(name, function, ALL.provide(function));
    }

    static <T> Key<T> of(Function<T, ?> function, IndexType type)
    {
        return type.provide(function, IndexType.MAP);
    }

    static <T> MapKey<T> of(Function<T, ?> function, MapIndexType type)
    {
        return new MapKey<>(function, type.provide(function));
    }

    static <T> MapKey<T> ofMap(Function<T, ?> function, MapIndexStrategy<T> strategy)
    {
        return new MapKey<>(function, strategy);
    }

    static <T> MapKey<T> ofMap(String name, Function<T, ?> function, MapIndexStrategy<T> strategy)
    {
        return new MapKey<>(name, function, strategy);
    }

    static <T> LuceneKey<T> ofLucene(Function<T, ?> function)
    {
        return new LuceneKey<>(function, new PartialMatchStrategy());
    }

    static <T> LuceneKey<T> ofLucene(String name, Function<T, ?> function)
    {
        return new LuceneKey<>(name, function, new PartialMatchStrategy());
    }

    static <T> LuceneKey<T> ofLucene(Function<T, ?> function, LuceneIndexStrategy strategy)
    {
        return new LuceneKey<>(function, strategy);
    }

    static <T> LuceneKey<T> ofLucene(String name, Function<T, ?> function, LuceneIndexStrategy strategy)
    {
        return new LuceneKey<>(name, function, strategy);
    }
}
