package tech.illuin.indexed.operator.lucene;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.indexed.Index;
import tech.illuin.indexed.IndexedStore;
import tech.illuin.indexed.MapStore;
import tech.illuin.indexed.key.Key;
import tech.illuin.indexed.operator.lucene.LuceneIndexTest.Indexable;
import tech.illuin.indexed.operator.lucene.strategy.FuzzyMatchStrategy;
import tech.illuin.indexed.operator.lucene.strategy.PartialMatchStrategy;

import java.util.List;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class LuceneStrategyTest
{
    public static final Key<Indexable> AB_PARTIAL = Key.ofLucene("AB", idx -> String.join(":", idx.a(), idx.b()), new PartialMatchStrategy());
    public static final Key<Indexable> AB_FUZZY = Key.ofLucene("AB", idx -> String.join(":", idx.a(), idx.b()), new FuzzyMatchStrategy());
    
    @Test
    public void testPartial__shouldMatchFully()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(AB_PARTIAL)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "12345", 1),
                new Indexable("value_a1", "123456", 2),
                new Indexable("value_a1", "1234567", 3)
            ));

            Assertions.assertEquals(3, store.getFirstMatch(new Indexable("value_a1", "1234567", 0), AB_PARTIAL).map(Indexable::c).orElse(0));
            Assertions.assertEquals(2, store.getFirstMatch(new Indexable("value_a1", "123456", 0), AB_PARTIAL).map(Indexable::c).orElse(0));
            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a1", "12345", 0), AB_PARTIAL).map(Indexable::c).orElse(0));
        }
    }

    @Test
    public void testPartial__shouldMatchFromFragment()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(AB_PARTIAL)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "123456", 1),
                new Indexable("value_a1", "12345678", 2),
                new Indexable("value_a1", "1234567890", 3)
            ));

            Assertions.assertEquals(3, store.getFirstMatch(new Indexable("value_a1", "123456789", 0), AB_PARTIAL).map(Indexable::c).orElse(0));
            Assertions.assertEquals(2, store.getFirstMatch(new Indexable("value_a1", "1234567", 0), AB_PARTIAL).map(Indexable::c).orElse(0));
            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a1", "12345", 0), AB_PARTIAL).map(Indexable::c).orElse(0));
        }
    }

    @Test
    public void testFuzzy__shouldMatch()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(AB_FUZZY)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "123456", 1),
                new Indexable("value_a1", "12345678", 2),
                new Indexable("value_a1", "1234567890", 3)
            ));

            Assertions.assertEquals(3, store.getFirstMatch(new Indexable("value_a2", "123456790", 0), AB_FUZZY).map(Indexable::c).orElse(0));
            Assertions.assertEquals(2, store.getFirstMatch(new Indexable("value_a3", "12345678", 0), AB_FUZZY).map(Indexable::c).orElse(0));
            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a4", "123256", 0), AB_FUZZY).map(Indexable::c).orElse(0));
        }
    }
}
