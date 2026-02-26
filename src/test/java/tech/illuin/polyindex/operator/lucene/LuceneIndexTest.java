package tech.illuin.polyindex.operator.lucene;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.polyindex.*;
import tech.illuin.polyindex.exception.UndefinedKeyException;
import tech.illuin.polyindex.key.Key;
import tech.illuin.polyindex.operator.lucene.strategy.FuzzyMatchStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static tech.illuin.polyindex.operator.lucene.LuceneIndexTest.Indexable.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class LuceneIndexTest
{
    @Test
    public void testOperator__shouldRejectNonLuceneKey()
    {
        try (LuceneOperator<Indexable> op = new LuceneOperator<>(Index.of(A_LUCENE), HashMap::new))
        {
            Indexable obj = new Indexable("string", "", 0);
            op.push(A_LUCENE, obj.a(), obj);

            Assertions.assertDoesNotThrow(() -> op.count(A_LUCENE));
            Assertions.assertThrows(IllegalArgumentException.class, () -> op.count(A));
            Assertions.assertThrows(UndefinedKeyException.class, () -> op.count(B_LUCENE));
        }
    }

    @Test
    public void testStore__shouldIndexValues()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(A, A_LUCENE, B_LUCENE, C_LUCENE, B_LUCENE_FUZZY)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "123456789", 12345),
                new Indexable("value_a2", "123456789", 23456),
                new Indexable("value_a3", "123456789", 23457),
                new Indexable("value_a4", "123457890", 23457),
                new Indexable("value_a5", "123456", 23458),
                new Indexable("value_a6", "12345", 23459),
                new Indexable("value_a7", "1234567", 23460)
            ));

            Assertions.assertTrue(store.contains("12345", B_LUCENE));
            Assertions.assertTrue(store.contains("123456789", B_LUCENE));
            Assertions.assertFalse(store.contains("34578", B_LUCENE));
            Assertions.assertFalse(store.contains("34578", B_LUCENE_FUZZY));
            Assertions.assertFalse(store.contains("19", B_LUCENE_FUZZY));

            Assertions.assertTrue(store.containsMatch(new Indexable(null, "123456", 0)));
            Assertions.assertFalse(store.containsMatch(new Indexable(null, "34578", 0)));

            Assertions.assertEquals(7, store.get("12345", B_LUCENE).size());
            Assertions.assertEquals(5, store.get("123456", B_LUCENE).size());
            Assertions.assertEquals(1, store.get("123457890", B_LUCENE).size());
        }
    }

    @Test
    public void testStore__shouldRespectIndexOrder()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(AB, AB_LUCENE, A, A_LUCENE)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "12345", 1),
                new Indexable("value_a1", "123456", 2),
                new Indexable("value_a1", "12345678", 3),
                new Indexable("value_a1", "1234567890", 4)
            ));

            Assertions.assertEquals(4, store.getFirstMatch(new Indexable("value_a1", "1234567890", 0), List.of(AB, AB_LUCENE, A, A_LUCENE)).map(Indexable::c).orElse(0));
            Assertions.assertEquals(4, store.getFirstMatch(new Indexable("value_a1", "123456789", 0), List.of(AB, AB_LUCENE, A, A_LUCENE)).map(Indexable::c).orElse(0));
            Assertions.assertEquals(3, store.getFirstMatch(new Indexable("value_a1", "12345678", 0), List.of(AB, AB_LUCENE, A, A_LUCENE)).map(Indexable::c).orElse(0));
            Assertions.assertEquals(3, store.getFirstMatch(new Indexable("value_a1", "1234567", 0), List.of(AB, AB_LUCENE, A, A_LUCENE)).map(Indexable::c).orElse(0));
            Assertions.assertEquals(2, store.getFirstMatch(new Indexable("value_a1", "123456", 0), List.of(AB, AB_LUCENE, A, A_LUCENE)).map(Indexable::c).orElse(0));
            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a1", "12345", 0), List.of(AB, AB_LUCENE, A, A_LUCENE)).map(Indexable::c).orElse(0));
            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a1", "23456", 0), List.of(AB, AB_LUCENE, A, A_LUCENE)).map(Indexable::c).orElse(0));
            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a1", "2345", 0), List.of(AB, AB_LUCENE, A, A_LUCENE)).map(Indexable::c).orElse(0));
            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a", "234", 0), List.of(AB, AB_LUCENE, A, A_LUCENE)).map(Indexable::c).orElse(0));
            Assertions.assertEquals(1, store.getFirstMatch(new Indexable("value_a", "23", 0), List.of(AB, AB_LUCENE, A, A_LUCENE)).map(Indexable::c).orElse(0));
        }
    }

    @Test
    public void testStore__removeShouldRemoveValue()
    {
        var idx1 = new Indexable("value_a1", "value_b1", 1234);
        var idx2 = new Indexable("value_a1", "value_b2", 2345);
        var idx3 = new Indexable("value_a1", "value_b2", 3456);
        var idx4 = new Indexable("value_a1", "value_b3", 4567);

        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(B_LUCENE)))
        {
            store.pushAll(List.of(idx1, idx2, idx3, idx4));

            Assertions.assertEquals(1, store.get("value_b1", B_LUCENE).size());
            Assertions.assertEquals(2, store.get("value_b2", B_LUCENE).size());
            Assertions.assertEquals(1, store.get("value_b3", B_LUCENE).size());
            Assertions.assertEquals(4, store.get("value_b", B_LUCENE).size());

            store.remove("value_b2", B_LUCENE);

            Assertions.assertEquals(1, store.get("value_b1", B_LUCENE).size());
            Assertions.assertEquals(0, store.get("value_b2", B_LUCENE).size());
            Assertions.assertEquals(1, store.get("value_b3", B_LUCENE).size());
            Assertions.assertEquals(2, store.get("value_b", B_LUCENE).size());

            store.removeMatch(new Indexable(null, "value_b", 0), B_LUCENE);

            Assertions.assertEquals(0, store.get("value_b1", B_LUCENE).size());
            Assertions.assertEquals(0, store.get("value_b2", B_LUCENE).size());
            Assertions.assertEquals(0, store.get("value_b3", B_LUCENE).size());
            Assertions.assertEquals(0, store.get("value_b4", B_LUCENE).size());
        }
    }

    @Test
    public void testStore__removeShouldThrowUndefinedKeyError()
    {
        var idx1 = new Indexable("value_a1", "value_b1", 1234);
        var idx2 = new Indexable("value_a1", "value_b2", 2345);
        var idx3 = new Indexable("value_a1", "value_b2", 3456);

        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(A_LUCENE, B_LUCENE)))
        {
            store.pushAll(List.of(idx1, idx2, idx3));
            Assertions.assertThrows(UndefinedKeyException.class, () -> store.remove("value_b1", A));
        }
    }

    @Test
    public void testStore__getAll()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(AB, A, A_LUCENE, B_LUCENE)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "value_b1", 1234),
                new Indexable("value_a2", "value_b1", 2345),
                new Indexable("value_a3", "value_b2", 2345)
            ));

            Assertions.assertEquals(3, store.getAll(AB).size());
            Assertions.assertEquals(3, store.getAll(A).size());
            Assertions.assertEquals(3, store.getAll(A_LUCENE).size());
            Assertions.assertEquals(3, store.getAll(B_LUCENE).size());
        }
    }

    @Test
    public void testStore__count()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(AB, A, A_LUCENE, B_LUCENE)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "value_b1", 1234),
                new Indexable("value_a2", "value_b1", 2345),
                new Indexable("value_a3", "value_b2", 2345)
            ));

            Assertions.assertEquals(3, store.count(AB));
            Assertions.assertEquals(3, store.count(A));
            Assertions.assertEquals(3, store.count(A_LUCENE));
            Assertions.assertEquals(3, store.count(B_LUCENE));
        }
    }

    @Test
    public void testStore__isEmpty()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(AB, A, A_LUCENE)))
        {
            Assertions.assertTrue(store.isEmpty());

            store.pushAll(List.of(
                new Indexable("value_a1", "value_b1", 1234),
                new Indexable("value_a2", "value_b1", 2345)
            ));

            Assertions.assertFalse(store.isEmpty());
        }
    }

    public record Indexable(String a, String b, int c)
    {
        public static final Key<Indexable> ABC = Key.of("ABC", idx -> String.join(":", idx.a(), idx.b(), Objects.toString(idx.c())));
        public static final Key<Indexable> AB = Key.of("AB", idx -> String.join(":", idx.a(), idx.b()));
        public static final Key<Indexable> A = Key.of("A", Indexable::a);
        public static final Key<Indexable> A_LUCENE = Key.of(Indexable::a, IndexType.LUCENE);
        public static final Key<Indexable> B_LUCENE = Key.ofLucene("B_LUCENE", Indexable::b);
        public static final Key<Indexable> C_LUCENE = Key.ofLucene("C_LUCENE", Indexable::c);
        public static final Key<Indexable> AB_LUCENE = Key.ofLucene("AB_LUCENE", idx -> String.join(":", idx.a(), idx.b()));
        public static final Key<Indexable> B_LUCENE_FUZZY = Key.ofLucene("B_LUCENE_FUZZY", Indexable::b, new FuzzyMatchStrategy());
    }
}
