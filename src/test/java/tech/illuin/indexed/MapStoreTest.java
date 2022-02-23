package tech.illuin.indexed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import tech.illuin.indexed.exception.UndefinedKeyException;

import java.util.List;
import java.util.Objects;

import static tech.illuin.indexed.MapStoreTest.Indexable.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MapStoreTest
{
    @Test
    public void testIndex__shouldRegister()
    {
        Index<Indexable> indexA = Index.of(A, B, C);
        Assertions.assertEquals(3, indexA.size());

        Index<Indexable> indexB = Index.of(A, B, B);
        Assertions.assertEquals(2, indexB.size());
    }

    @Test
    public void testStore__shouldIndexValues()
    {
        IndexedStore<Indexable> store = new MapStore<>(Index.of(A, B, C)).pushAll(List.of(
            new Indexable("value_a1", "value_b1", 1234),
            new Indexable("value_a2", "value_b1", 2345),
            new Indexable("value_a3", "value_b2", 2345)
        ));

        Assertions.assertTrue(store.contains("value_a1", A));
        Assertions.assertTrue(store.contains("value_a2", A));
        Assertions.assertTrue(store.contains("value_a3", A));
        Assertions.assertEquals(1, store.get("value_a1", A).size());
        Assertions.assertEquals(1, store.get("value_a2", A).size());
        Assertions.assertEquals(1, store.get("value_a3", A).size());

        Assertions.assertTrue(store.contains("value_b1", B));
        Assertions.assertTrue(store.contains("value_b2", B));
        Assertions.assertEquals(2, store.get("value_b1", B).size());
        Assertions.assertEquals(1, store.get("value_b2", B).size());

        Assertions.assertTrue(store.contains(1234, C));
        Assertions.assertTrue(store.contains(2345, C));
        Assertions.assertEquals(1, store.get(1234, C).size());
        Assertions.assertEquals(2, store.get(2345, C).size());
    }

    @Test
    public void testStore__shouldIndexValuesCombined()
    {
        var idx1 = new Indexable("value_a1", "value_b1", 1234);
        var idx2 = new Indexable("value_a2", "value_b1", 2345);
        var idx3 = new Indexable("value_a3", "value_b2", 2345);

        IndexedStore<Indexable> store = new MapStore<>(Index.of(ABC, AB, A)).pushAll(List.of(idx1, idx2, idx3));

        Assertions.assertTrue(store.containsMatch(idx1, A));
        Assertions.assertTrue(store.containsMatch(idx1, AB));
        Assertions.assertTrue(store.containsMatch(idx1, ABC));
        Assertions.assertFalse(store.containsMatch(idx1, B));

        var idxMatch = new Indexable("value_a3", "value_b2", 0);
        Assertions.assertTrue(store.containsMatch(idxMatch));
        Assertions.assertTrue(store.containsMatch(idxMatch, A));
        Assertions.assertTrue(store.containsMatch(idxMatch, AB));
        Assertions.assertFalse(store.containsMatch(idxMatch, ABC));
        Assertions.assertFalse(store.containsMatch(idx1, B));
    }

    @Test
    public void testStore__shouldIndexValuesUnique()
    {
        var idx1 = new Indexable("value_a1", "value_b1", 1234);
        var idx2 = new Indexable("value_a1", "value_b1", 2345);
        var idx3 = new Indexable("value_a1", "value_b2", 3456);
        var idx4 = new Indexable("value_a2", "value_b2", 4567);
        var idx5 = new Indexable("value_a2", "value_b2", 5678);

        IndexedStore<Indexable> store = new MapStore<>(Index.of(A, AB, A_UNIQUE, AB_UNIQUE)).pushAll(List.of(idx1, idx2, idx3, idx4, idx5));

        Assertions.assertTrue(store.containsMatch(idx1, A));
        Assertions.assertTrue(store.containsMatch(idx1, AB));
        Assertions.assertTrue(store.containsMatch(idx1, A_UNIQUE));
        Assertions.assertTrue(store.containsMatch(idx1, AB_UNIQUE));
        Assertions.assertFalse(store.containsMatch(idx1, B));

        var idxMatch1 = new Indexable("value_a1", "value_b1", 0);
        Assertions.assertTrue(store.containsMatch(idxMatch1));
        Assertions.assertEquals(3, store.getMatch(idxMatch1, A).size());
        Assertions.assertEquals(2, store.getMatch(idxMatch1, AB).size());
        Assertions.assertEquals(1, store.getMatch(idxMatch1, A_UNIQUE).size());
        Assertions.assertEquals(1, store.getMatch(idxMatch1, AB_UNIQUE).size());
        Assertions.assertEquals(1234, store.getFirstMatch(idxMatch1, A_UNIQUE).map(Indexable::c).orElse(-1));
        Assertions.assertEquals(2345, store.getFirstMatch(idxMatch1, AB_UNIQUE).map(Indexable::c).orElse(-1));

        var idxMatch2 = new Indexable("value_a1", "value_b2", 0);
        Assertions.assertTrue(store.containsMatch(idxMatch2));
        Assertions.assertEquals(3, store.getMatch(idxMatch2, A).size());
        Assertions.assertEquals(1, store.getMatch(idxMatch2, AB).size());
        Assertions.assertEquals(1, store.getMatch(idxMatch2, A_UNIQUE).size());
        Assertions.assertEquals(1, store.getMatch(idxMatch2, AB_UNIQUE).size());
        Assertions.assertEquals(1234, store.getFirstMatch(idxMatch2, A_UNIQUE).map(Indexable::c).orElse(-1));
        Assertions.assertEquals(3456, store.getFirstMatch(idxMatch2, AB_UNIQUE).map(Indexable::c).orElse(-1));

        var idxMatch3 = new Indexable("value_a2", "value_b2", 0);
        Assertions.assertTrue(store.containsMatch(idxMatch3));
        Assertions.assertEquals(2, store.getMatch(idxMatch3, A).size());
        Assertions.assertEquals(2, store.getMatch(idxMatch3, AB).size());
        Assertions.assertEquals(1, store.getMatch(idxMatch3, A_UNIQUE).size());
        Assertions.assertEquals(1, store.getMatch(idxMatch3, AB_UNIQUE).size());
        Assertions.assertEquals(4567, store.getFirstMatch(idxMatch3, A_UNIQUE).map(Indexable::c).orElse(-1));
        Assertions.assertEquals(5678, store.getFirstMatch(idxMatch3, AB_UNIQUE).map(Indexable::c).orElse(-1));
    }

    @Test
    public void testStore__shouldRespectIndexOrder()
    {
        var idx1 = new Indexable("value_a1", "value_b1", 1234);
        var idx2 = new Indexable("value_a1", "value_b2", 2345);
        var idx3 = new Indexable("value_a1", "value_b2", 3456);

        IndexedStore<Indexable> store = new MapStore<>(Index.of(ABC, AB, A)).pushAll(List.of(idx1, idx2, idx3));

        var idxMatch1 = new Indexable("value_a1", "value_b1", 0);
        var idxMatch2 = new Indexable("value_a1", "value_b2", 0);
        var idxMatch3 = new Indexable("value_a1", "value_b2", 3456);

        var idxFound1 = store.getFirstMatch(idxMatch1, List.of(ABC, AB, A));
        Assertions.assertDoesNotThrow((Executable) idxFound1::orElseThrow);
        Assertions.assertEquals(idx1, idxFound1.orElse(null));

        var idxFound2 = store.getFirstMatch(idxMatch2, List.of(ABC, AB, A));
        Assertions.assertDoesNotThrow((Executable) idxFound2::orElseThrow);
        Assertions.assertEquals(idx2, idxFound2.orElse(null));

        var idxFound3 = store.getFirstMatch(idxMatch3, List.of(ABC, AB, A));
        Assertions.assertDoesNotThrow((Executable) idxFound3::orElseThrow);
        Assertions.assertEquals(idx3, idxFound3.orElse(null));
    }

    @Test
    public void testStore__removeShouldRemoveValue()
    {
        var idx1 = new Indexable("value_a1", "value_b1", 1234);
        var idx2 = new Indexable("value_a1", "value_b2", 2345);
        var idx3 = new Indexable("value_a1", "value_b2", 3456);

        IndexedStore<Indexable> store = new MapStore<>(Index.of(A, B)).pushAll(List.of(idx1, idx2, idx3));

        Assertions.assertEquals(1, store.get("value_b1", B).size());
        Assertions.assertEquals(2, store.get("value_b2", B).size());

        store.remove("value_b2", B);

        Assertions.assertEquals(1, store.get("value_b1", B).size());
        Assertions.assertEquals(0, store.get("value_b2", B).size());
    }

    @Test
    public void testStore__removeShouldThrowUndefinedKeyError()
    {
        var idx1 = new Indexable("value_a1", "value_b1", 1234);
        var idx2 = new Indexable("value_a1", "value_b2", 2345);
        var idx3 = new Indexable("value_a1", "value_b2", 3456);

        IndexedStore<Indexable> store = new MapStore<>(Index.of(A, AB)).pushAll(List.of(idx1, idx2, idx3));

        Assertions.assertThrows(UndefinedKeyException.class, () -> store.remove("value_b1", B));
    }

    public record Indexable(String a, String b, int c)
    {
        public static final Key<Indexable> ABC = Key.of(idx -> String.join(":", idx.a(), idx.b(), Objects.toString(idx.c())));
        public static final Key<Indexable> AB = Key.of(idx -> String.join(":", idx.a(), idx.b()));
        public static final Key<Indexable> A = Key.of(Indexable::a);
        public static final Key<Indexable> B = Key.of(Indexable::b);
        public static final Key<Indexable> C = Key.of(Indexable::c);
        public static final Key<Indexable> A_UNIQUE = Key.of(Indexable::a, IndexingType.FIRST);
        public static final Key<Indexable> AB_UNIQUE = Key.of(idx -> String.join(":", idx.a(), idx.b()), IndexingType.LAST);
    }
}
