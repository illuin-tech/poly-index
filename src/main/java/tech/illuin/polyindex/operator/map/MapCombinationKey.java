package tech.illuin.polyindex.operator.map;

import tech.illuin.polyindex.operator.map.strategy.MapIndexStrategy;
import tech.illuin.polyindex.query.IndexKeyCollection;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Combination keys are a shorthand type of {@link MapKey} which produces keys by combining the return values of one or several functions,
 * the "required" list, applied to the object being indexed.
 * Values are then combined into a simple hash separated by a delimiter, only if all the "required" values are not-null.<br/>
 * <br/>
 * Additionally, the key can be initialized with an "excluded" list, which is used for validating the input being indexed, only if all the "excluded" values are null.<br/>
 * <br/>
 * This can be seen as an alternative to manually using different indexing and querying hash strategies for the same {@link MapKey}, e.g.:
 * <ul>
 *     <li>the indexing hash would contain "required" + "excluded"</li>
 *     <li>the querying hash would contain "required" + empty placeholders for "excluded" values</li>
 * </ul>
 *
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class MapCombinationKey<T> extends MapKey<T>
{
    private static final String DEFAULT_DELIMITER = ":";

    private MapCombinationKey(Function<T, ?> indexingFunction, Function<T, ?> queryingFunction, MapIndexType type)
    {
        super(indexingFunction, type.provide(queryingFunction));
    }

    private MapCombinationKey(Function<T, ?> function, MapIndexStrategy<T> strategy)
    {
        super(function, strategy);
    }

    public static <T> MapCombinationKey<T> of(List<Variant<T>> variants, MapIndexType type)
    {
        return new MapCombinationKey<>(createIndexingFunction(variants), createQueryingFunction(variants), type);
    }

    public static <T> MapCombinationKey<T> of(List<Variant<T>> variants, MapIndexStrategy<T> strategy)
    {
        return new MapCombinationKey<>(createIndexingFunction(variants), strategy);
    }

    public static <T> MapCombinationKey<T> of(Requirements<T> required, MapIndexType type)
    {
        return of(List.of(variant(required)), type);
    }

    public static <T> MapCombinationKey<T> of(Requirements<T> required, Exclusions<T> excluded, MapIndexType type)
    {
        return of(List.of(variant(required, excluded)), type);
    }

    public static <T> MapCombinationKey<T> of(Requirements<T> required, Exclusions<T> excluded, MapIndexStrategy<T> strategy)
    {
        return of(List.of(variant(required, excluded)), strategy);
    }

    private static <T> Function<T, ?> createIndexingFunction(List<Variant<T>> variants)
    {
        return in -> IndexKeyCollection.of(variants.stream()
            .map(v -> joinIfNotNull(
                DEFAULT_DELIMITER,
                v.requirements().stream().map(kc -> kc.supply(in)).toList(),
                v.exclusions().stream().map(kc -> kc.supply(in)).collect(Collectors.toSet())
            ))
            .toList()
        );
    }

    private static <T> Function<T, ?> createQueryingFunction(List<Variant<T>> variants)
    {
        return in -> IndexKeyCollection.of(variants.stream()
            .map(v -> joinIfNotNull(
                DEFAULT_DELIMITER,
                v.requirements().stream().map(kc -> kc.supply(in)).toList()
            ))
            .toList()
        );
    }

    public static <T> Variant<T> variant(Requirements<T> requirements)
    {
        return new Variant<>(requirements, excludes());
    }

    public static <T> Variant<T> variant(Requirements<T> requirements, Exclusions<T> exclusions)
    {
        return new Variant<>(requirements, exclusions);
    }

    @SafeVarargs
    public static <T> Requirements<T> requires(KeyComponent<T>... components)
    {
        return new Requirements<>(Arrays.asList(components));
    }

    @SafeVarargs
    public static <T> Exclusions<T> excludes(KeyComponent<T>... components)
    {
        return new Exclusions<>(Arrays.asList(components));
    }

    private static String joinIfNotNull(String delimiter, List<String> args)
    {
        return joinIfNotNull(delimiter, args, Collections.emptySet());
    }

    private static String joinIfNotNull(String delimiter, List<String> args, Set<String> requireNulls)
    {
        if (args.isEmpty())
            return null;
        if (requireNulls.stream().anyMatch(Objects::nonNull))
            return null;
        if (args.stream().anyMatch(Objects::isNull))
            return null;
        return String.join(delimiter, args);
    }

    @FunctionalInterface
    public interface KeyComponent<T>
    {
        String supply(T input);
    }

    public static final class Variant<T>
    {
        private final Requirements<T> requirements;
        private final Exclusions<T> exclusions;

        private Variant(Requirements<T> requirements, Exclusions<T> exclusions)
        {
            this.requirements = requirements;
            this.exclusions = exclusions;
        }

        public Requirements<T> requirements()
        {
            return this.requirements;
        }

        public Exclusions<T> exclusions()
        {
            return this.exclusions;
        }
    }

    public static final class Requirements<T>
    {
        private final List<KeyComponent<T>> components;

        private Requirements(Collection<KeyComponent<T>> components)
        {
            this.components = new ArrayList<>(components);
        }

        public Stream<KeyComponent<T>> stream()
        {
            return this.components.stream();
        }
    }

    public static final class Exclusions<T>
    {
        private final Set<KeyComponent<T>> components;

        private Exclusions(Collection<KeyComponent<T>> components)
        {
            this.components = new HashSet<>(components);
        }

        public Stream<KeyComponent<T>> stream()
        {
            return this.components.stream();
        }
    }
}
