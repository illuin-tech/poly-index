package tech.illuin.polyindex.operator.map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.polyindex.*;
import tech.illuin.polyindex.exception.UndefinedKeyException;
import tech.illuin.polyindex.key.Key;
import tech.illuin.polyindex.operator.map.strategy.IndexAllStrategy;
import tech.illuin.polyindex.operator.map.strategy.IndexFirstStrategy;
import tech.illuin.polyindex.operator.map.strategy.IndexLastStrategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static tech.illuin.polyindex.operator.map.MapIndexTest.Indexable.*;
import static tech.illuin.polyindex.operator.map.MapIndexType.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MapIndexTest
{
    @Test
    public void testOperator__shouldRejectNonMapKey()
    {
        try (MapOperator<Indexable> op = new MapOperator<>(Index.of(B_MAP), HashMap::new))
        {
            Indexable obj = new Indexable("string", "", 0);
            op.push(B_MAP, obj.a(), obj);

            Assertions.assertDoesNotThrow(() -> op.count(B_MAP));
            Assertions.assertThrows(IllegalArgumentException.class, () -> op.count(B_LUCENE));
            Assertions.assertThrows(UndefinedKeyException.class, () -> op.count(B_PARTIAL_MAP));
        }
    }

    @Test
    public void test_shouldBuildAppropriateKeys()
    {
        Assertions.assertInstanceOf(MapKey.class, A_BASIC);
        Assertions.assertInstanceOf(IndexAllStrategy.class, ((MapKey<Indexable>) A_BASIC).strategy());

        Assertions.assertInstanceOf(MapKey.class, A_BASIC_ALL);
        Assertions.assertInstanceOf(IndexAllStrategy.class, ((MapKey<Indexable>) A_BASIC_ALL).strategy());

        Assertions.assertInstanceOf(MapKey.class, A_BASIC_FIRST);
        Assertions.assertInstanceOf(IndexFirstStrategy.class, ((MapKey<Indexable>) A_BASIC_FIRST).strategy());

        Assertions.assertInstanceOf(MapKey.class, A_BASIC_LAST);
        Assertions.assertInstanceOf(IndexLastStrategy.class, ((MapKey<Indexable>) A_BASIC_LAST).strategy());
    }

    @Test
    public void testStore__shouldIndexValues()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(B_MAP, B_PARTIAL_MAP, D_MULTI_MAP)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "123456789", 12345, List.of("a0", "b0", "c0")),
                new Indexable("value_a2", "123456789", 23456, List.of("a1", "b1", "c1")),
                new Indexable("value_a3", "123456789", 23457, List.of("a2", "b2", "c2")),
                new Indexable("value_a4", "123457890", 23457, List.of("a2", "b2", "c2")),
                new Indexable("value_a5", "123456", 23458),
                new Indexable("value_a6", "12345", 23459),
                new Indexable("value_a7", "1234567", 23460)
            ));

            Assertions.assertTrue(store.contains("12345", B_MAP));
            Assertions.assertFalse(store.contains("123", B_MAP));
            Assertions.assertTrue(store.contains("123", B_PARTIAL_MAP));
            Assertions.assertFalse(store.contains("34578", B_MAP));
            Assertions.assertFalse(store.contains("34578", B_PARTIAL_MAP));
            Assertions.assertTrue(store.contains("a0", D_MULTI_MAP));
            Assertions.assertTrue(store.contains("b0", D_MULTI_MAP));
            Assertions.assertTrue(store.contains("c0", D_MULTI_MAP));
            Assertions.assertFalse(store.contains(List.of("a0", "b0", "c0"), D_MULTI_MAP));
            Assertions.assertTrue(store.contains("a1", D_MULTI_MAP));
            Assertions.assertTrue(store.contains("b1", D_MULTI_MAP));
            Assertions.assertTrue(store.contains("c1", D_MULTI_MAP));
            Assertions.assertFalse(store.contains(List.of("a1", "b1", "c1"), D_MULTI_MAP));
            Assertions.assertTrue(store.contains("a2", D_MULTI_MAP));
            Assertions.assertTrue(store.contains("b2", D_MULTI_MAP));
            Assertions.assertTrue(store.contains("c2", D_MULTI_MAP));
            Assertions.assertFalse(store.contains(List.of("a2", "b2", "c2"), D_MULTI_MAP));

            Assertions.assertTrue(store.containsMatch(new Indexable(null, "123456", 0)));
            Assertions.assertTrue(store.containsMatch(new Indexable(null, "123", 0)));
            Assertions.assertFalse(store.containsMatch(new Indexable(null, "12345678", 0, List.of("a3", "b3", "c3", "d3"))));

            Assertions.assertEquals(1, store.get("12345", B_MAP).size());
            Assertions.assertEquals(1, store.get("123456", B_MAP).size());
            Assertions.assertEquals(1, store.get("123", B_PARTIAL_MAP).size());
            Assertions.assertEquals(1, store.get("a0", D_MULTI_MAP).size());
            Assertions.assertEquals(2, store.get("a2", D_MULTI_MAP).size());
        }
    }

    public record Indexable(String a, String b, int c, List<String> d)
    {
        public static final Key<Indexable> A_BASIC = Key.of(Indexable::a);
        public static final Key<Indexable> A_BASIC_ALL = Key.of(Indexable::a, ALL);
        public static final Key<Indexable> A_BASIC_FIRST = Key.of(Indexable::a, FIRST);
        public static final Key<Indexable> A_BASIC_LAST = Key.of(Indexable::a, LAST);

        public static final Key<Indexable> B_LUCENE = Key.of(Indexable::b, IndexType.LUCENE);
        public static final Key<Indexable> B_MAP = Key.of(Indexable::b, IndexType.MAP);
        public static final Key<Indexable> B_PARTIAL_MAP = Key.ofMap(
            idx -> idx.b().substring(0, idx.b().length() - 2),
            new IndexAllStrategy<>(Indexable::b)
        );

        public static final Key<Indexable> D_MULTI_MAP = Key.of(Indexable::d);

        public Indexable(String a, String b, int c)
        {
            this(a, b, c, Collections.emptyList());
        }
    }
}
