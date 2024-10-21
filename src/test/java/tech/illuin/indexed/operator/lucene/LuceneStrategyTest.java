package tech.illuin.indexed.operator.lucene;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
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
import java.util.regex.Matcher;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class LuceneStrategyTest
{
    public static final Key<Indexable> AB_PARTIAL = Key.ofLucene("AB", idx -> String.join(":", idx.a(), idx.b()), new PartialMatchStrategy());
    public static final Key<Indexable> AB_FUZZY = Key.ofLucene("AB", idx -> String.join(":", idx.a(), idx.b()), new FuzzyMatchStrategy());
    public static final Key<Indexable> B_FUZZY_STRICT = Key.ofLucene("B", Indexable::b, new FuzzyMatchStrategy(opts -> opts.setMaxDistance(1)));
    public static final Key<Indexable> A_AND_B_QUERY = Key.ofLuceneQuery(
        LuceneStrategyTest::createFields,
        (parser, idx) -> parser.parse(String.format("a:%s AND b:%s", idx.a(), idx.b()))
    );
    public static final Key<Indexable> A_AND_B_PARTIAL_QUERY = Key.ofLuceneQuery(
        LuceneStrategyTest::createFields,
        (parser, idx) -> parser.parse(String.format("a:%s AND b:%s*", idx.a(), idx.b()))
    );
    public static final Key<Indexable> A_AND_B_OR_C_QUERY = Key.ofLuceneQuery(
        LuceneStrategyTest::createFields,
        (parser, idx) -> new BooleanQuery.Builder()
            .add(new TermQuery(new Term("a", idx.a())), Occur.MUST)
            .add(new BooleanQuery.Builder()
                .add(new TermQuery(new Term("b", idx.b())), Occur.SHOULD)
                .add(IntPoint.newExactQuery("c", idx.c()), Occur.SHOULD)
                .build(), Occur.MUST
            )
            .build()
    );

    private static List<Field> createFields(Indexable idx)
    {
        return List.of(
            new StringField("a", idx.a(), Field.Store.NO),
            new TextField("b", idx.b(), Field.Store.YES),
            new IntPoint("c", idx.c())
        );
    }

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

    @Test
    public void testFuzzyString__shouldMatch()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(B_FUZZY_STRICT)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "123456", 1),
                new Indexable("value_a1", "123456780", 2),
                new Indexable("value_a1", "1234567890", 3)
            ));

            Assertions.assertEquals(2, store.getFirstMatch(new Indexable(null, "123456790", 0), B_FUZZY_STRICT).map(Indexable::c).orElse(0));
            Assertions.assertEquals(2, store.getFirstMatch(new Indexable(null, "12345678", 0), B_FUZZY_STRICT).map(Indexable::c).orElse(0));
            Assertions.assertEquals(1, store.getFirstMatch(new Indexable(null, "123256", 0), B_FUZZY_STRICT).map(Indexable::c).orElse(0));
        }
    }

    @Test
    public void testQuery__and__shouldMatch()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(A_AND_B_QUERY)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "123456", 1),
                new Indexable("value_a1", "123456780", 2),
                new Indexable("value_a1", "1234567890", 3)
            ));

            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a1", "123456", 0), A_AND_B_QUERY).map(Indexable::c).orElse(0));
            Assertions.assertEquals(2, store.getFirstMatch(new Indexable("value_a1", "123456780", 0), A_AND_B_QUERY).map(Indexable::c).orElse(0));
            Assertions.assertEquals(3, store.getFirstMatch(new Indexable("value_a1", "1234567890", 0), A_AND_B_QUERY).map(Indexable::c).orElse(0));
        }
    }

    @Test
    public void testQuery__andPartial__shouldMatch()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(A_AND_B_PARTIAL_QUERY)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "123456", 1),
                new Indexable("value_a1", "123456780", 2),
                new Indexable("value_a1", "1234567890", 3)
            ));

            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a1", "1234", 0), A_AND_B_PARTIAL_QUERY).map(Indexable::c).orElse(0));
            Assertions.assertEquals(2, store.getFirstMatch(new Indexable("value_a1", "1234567", 0), A_AND_B_PARTIAL_QUERY).map(Indexable::c).orElse(0));
            Assertions.assertEquals(3, store.getFirstMatch(new Indexable("value_a1", "123456789", 0), A_AND_B_PARTIAL_QUERY).map(Indexable::c).orElse(0));
        }
    }

    @Test
    public void testQuery__and_or__shouldMatch()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(A_AND_B_OR_C_QUERY)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "123456", 1),
                new Indexable("value_a1", "123456780", 2),
                new Indexable("value_a1", "1234567890", 3)
            ));

            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a1", "123456", 0), A_AND_B_OR_C_QUERY).map(Indexable::c).orElse(0));
            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a1", "234567", 1), A_AND_B_OR_C_QUERY).map(Indexable::c).orElse(0));
            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a1", "123456", 1), A_AND_B_OR_C_QUERY).map(Indexable::c).orElse(0));
            Assertions.assertEquals(0, store.getFirstMatch(new Indexable("value_b2", "123456", 1), A_AND_B_OR_C_QUERY).map(Indexable::c).orElse(0));

            Assertions.assertEquals(2, store.getFirstMatch(new Indexable("value_a1", "123456780", 0), A_AND_B_OR_C_QUERY).map(Indexable::c).orElse(0));
            Assertions.assertEquals(2, store.getFirstMatch(new Indexable("value_a1", "234567890", 2), A_AND_B_OR_C_QUERY).map(Indexable::c).orElse(0));
        }
    }
}
