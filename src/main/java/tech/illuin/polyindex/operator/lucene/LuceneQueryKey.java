package tech.illuin.polyindex.operator.lucene;

import tech.illuin.polyindex.operator.lucene.strategy.MultiFieldMatchStrategy;
import tech.illuin.polyindex.operator.lucene.strategy.MultiFieldMatchStrategy.FieldFunction;
import tech.illuin.polyindex.operator.lucene.strategy.MultiFieldMatchStrategy.QueryFunction;

public class LuceneQueryKey<T> extends LuceneKey<T>
{
    public LuceneQueryKey(FieldFunction<T> fieldFunction, QueryFunction<T> queryFunction)
    {
        this(null, fieldFunction, queryFunction);
    }

    public LuceneQueryKey(String name, FieldFunction<T> fieldFunction, QueryFunction<T> queryFunction)
    {
        super(name, v -> v, new MultiFieldMatchStrategy<>(fieldFunction, queryFunction));
    }
}
