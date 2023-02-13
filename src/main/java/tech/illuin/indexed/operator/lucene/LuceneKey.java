package tech.illuin.indexed.operator.lucene;

import tech.illuin.indexed.IndexingType;
import tech.illuin.indexed.key.BasicKey;
import tech.illuin.indexed.operator.lucene.strategy.IndexStrategy;
import tech.illuin.indexed.operator.lucene.strategy.PartialMatchStrategy;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class LuceneKey<T> extends BasicKey<T>
{
    private final IndexStrategy strategy;

    public LuceneKey(Function<T, ?> function)
    {
        this(function, new PartialMatchStrategy());
    }

    public LuceneKey(Function<T, ?> function, IndexStrategy strategy)
    {
        this(null, function, strategy);
    }

    public LuceneKey(String name, Function<T, ?> function, IndexStrategy strategy)
    {
        super(name, function, IndexingType.LUCENE);
        this.strategy = strategy;
    }

    public IndexStrategy strategy()
    {
        return this.strategy;
    }
}
