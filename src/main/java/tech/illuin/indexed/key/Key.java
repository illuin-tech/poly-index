package tech.illuin.indexed.key;

import tech.illuin.indexed.IndexingType;
import tech.illuin.indexed.operator.IndexFamily;
import tech.illuin.indexed.operator.lucene.LuceneKey;
import tech.illuin.indexed.operator.lucene.strategy.IndexStrategy;
import tech.illuin.indexed.operator.lucene.strategy.PartialMatchStrategy;
import tech.illuin.indexed.query.Query;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface Key<T>
{
    Object compute(T value);

    IndexingType type();

    String name();

    default Query<T> match(T value)
    {
        return new Query<>(this, this.compute(value));
    }

    default Query<T> query(Object value)
    {
        return new Query<>(this, value);
    }

    default IndexFamily family()
    {
        return this.type().family();
    }

    static <T> Key<T> of(Function<T, ?> function)
    {
        return new BasicKey<>(function, IndexingType.ALL);
    }

    static <T> Key<T> of(String name, Function<T, ?> function)
    {
        return new BasicKey<>(name, function, IndexingType.ALL);
    }

    static <T> Key<T> of(Function<T, ?> function, IndexingType type)
    {
        return type.family().provide(function, type);
    }

    static <T> Key<T> ofLucene(Function<T, ?> function)
    {
        return new LuceneKey<>(function, new PartialMatchStrategy());
    }

    static <T> Key<T> ofLucene(String name, Function<T, ?> function)
    {
        return new LuceneKey<>(name, function, new PartialMatchStrategy());
    }

    static <T> Key<T> ofLucene(Function<T, ?> function, IndexStrategy strategy)
    {
        return new LuceneKey<>(function, strategy);
    }

    static <T> Key<T> ofLucene(String name, Function<T, ?> function, IndexStrategy strategy)
    {
        return new LuceneKey<>(name, function, strategy);
    }
}
