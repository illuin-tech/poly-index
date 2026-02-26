package tech.illuin.polyindex.operator.lucene;

import tech.illuin.polyindex.IndexType;
import tech.illuin.polyindex.key.BasicKey;
import tech.illuin.polyindex.operator.lucene.strategy.LuceneIndexStrategy;
import tech.illuin.polyindex.operator.lucene.strategy.PartialMatchStrategy;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class LuceneKey<T> extends BasicKey<T>
{
    private final LuceneIndexStrategy strategy;

    public LuceneKey(Function<T, ?> function)
    {
        this(function, new PartialMatchStrategy());
    }

    public LuceneKey(Function<T, ?> function, LuceneIndexStrategy strategy)
    {
        this(null, function, strategy);
    }

    public LuceneKey(String name, Function<T, ?> function, LuceneIndexStrategy strategy)
    {
        super(name, function, IndexType.LUCENE);
        this.strategy = strategy;
    }

    public LuceneIndexStrategy strategy()
    {
        return this.strategy;
    }
}
