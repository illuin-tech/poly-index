package tech.illuin.indexed.operator;

import tech.illuin.indexed.IndexingType;
import tech.illuin.indexed.key.BasicKey;
import tech.illuin.indexed.key.Key;
import tech.illuin.indexed.operator.lucene.LuceneKey;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public enum IndexFamily
{
    MAP(BasicKey::new),
    LUCENE((SpecificProvider) LuceneKey::new),
    ;

    private final ReferenceProvider provider;

    IndexFamily(ReferenceProvider provider)
    {
        this.provider = provider;
    }

    public <T> Key<T> provide(Function<T, ?> function, IndexingType type)
    {
        return this.provider.provide(function, type);
    }

    private interface SpecificProvider extends ReferenceProvider
    {
        <T> Key<T> provide(Function<T, ?> function);

        @Override
        default <T> Key<T> provide(Function<T, ?> function, IndexingType type)
        {
            return this.provide(function);
        }
    }
}
