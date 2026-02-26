package tech.illuin.polyindex;

import tech.illuin.polyindex.key.Key;
import tech.illuin.polyindex.operator.ReferenceProvider;
import tech.illuin.polyindex.operator.lucene.LuceneKey;
import tech.illuin.polyindex.operator.map.MapKey;

import java.util.function.Function;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public enum IndexType
{
    MAP((SpecificProvider) MapKey::new),
    LUCENE((SpecificProvider) LuceneKey::new),
    ;

    private final ReferenceProvider provider;

    IndexType(ReferenceProvider provider)
    {
        this.provider = provider;
    }

    public <T> Key<T> provide(Function<T, ?> function, IndexType type)
    {
        return this.provider.provide(function, type);
    }

    private interface SpecificProvider extends ReferenceProvider
    {
        <T> Key<T> provide(Function<T, ?> function);

        @Override
        default <T> Key<T> provide(Function<T, ?> function, IndexType type)
        {
            return this.provide(function);
        }
    }
}
