package tech.illuin.polyindex.operator.map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.illuin.polyindex.Index;
import tech.illuin.polyindex.IndexedStore;
import tech.illuin.polyindex.MapStore;
import tech.illuin.polyindex.key.Key;

import java.util.List;

import static tech.illuin.polyindex.operator.map.MapCombinationIndexTest.Indexable.*;
import static tech.illuin.polyindex.operator.map.MapCombinationKey.excludes;
import static tech.illuin.polyindex.operator.map.MapCombinationKey.requires;
import static tech.illuin.polyindex.operator.map.MapIndexType.*;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MapCombinationIndexTest
{
    @Test
    public void testStore__shouldIndexValues()
    {
        try (IndexedStore<Indexable> store = new MapStore<>(Index.of(REQUIRE_B, REQUIRE_B_C, REQUIRE_B_EXCLUDE_C)))
        {
            store.pushAll(List.of(
                new Indexable("value_a1", "123456789", "abcde"),
                new Indexable("value_a2", null, "fghij"),
                new Indexable("value_a3", "123456789",  null),
                new Indexable("value_a4", null, "fghij"),
                new Indexable("value_a5", "123456", "klmno"),
                new Indexable("value_a6", "12345", null),
                new Indexable("value_a7", "1234567", "klmno")
            ));

            Assertions.assertEquals(5, store.count(REQUIRE_B));
            Assertions.assertEquals(3, store.count(REQUIRE_B_C));
            Assertions.assertEquals(2, store.count(REQUIRE_B_EXCLUDE_C));

            Assertions.assertEquals("value_a1", store.getFirstMatch(new Indexable(null, "123456789", null), REQUIRE_B).map(Indexable::a).orElse(null));
            Assertions.assertTrue(store.getFirstMatch(new Indexable(null, "123456789", null), REQUIRE_B_C).isEmpty());
            Assertions.assertEquals("value_a3", store.getFirstMatch(new Indexable(null, "123456789", null), REQUIRE_B_EXCLUDE_C).map(Indexable::a).orElse(null));

            Assertions.assertEquals("value_a1", store.getFirstMatch(new Indexable(null, "123456789", "abcde"), REQUIRE_B).map(Indexable::a).orElse(null));
            Assertions.assertEquals("value_a1", store.getFirstMatch(new Indexable(null, "123456789", "abcde"), REQUIRE_B_C).map(Indexable::a).orElse(null));
            Assertions.assertEquals("value_a3", store.getFirstMatch(new Indexable(null, "123456789", "abcde"), REQUIRE_B_EXCLUDE_C).map(Indexable::a).orElse(null));
        }
    }

    public record Indexable(String a, String b, String c)
    {
        public static final Key<Indexable> REQUIRE_B = MapCombinationKey.of(requires(Indexable::b), ALL);
        public static final Key<Indexable> REQUIRE_B_C = MapCombinationKey.of(requires(Indexable::b, Indexable::c), ALL);
        public static final Key<Indexable> REQUIRE_B_EXCLUDE_C = MapCombinationKey.of(
            requires(Indexable::b),
            excludes(Indexable::c),
            ALL
        );
    }
}
