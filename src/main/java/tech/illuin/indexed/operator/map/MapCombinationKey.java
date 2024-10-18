package tech.illuin.indexed.operator.map;

import tech.illuin.indexed.operator.map.strategy.MapIndexStrategy;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static <T> MapCombinationKey<T> of(Requirements<T> required, MapIndexType type)
    {
        return of(required, excludes(), type);
    }

    public static <T> MapCombinationKey<T> of(Requirements<T> required, Exclusions<T> excluded, MapIndexType type)
    {
        return new MapCombinationKey<>(createIndexingFunction(required, excluded), createQueryingFunction(required), type);
    }

    public static <T> MapCombinationKey<T> of(Requirements<T> required, Exclusions<T> excluded, MapIndexStrategy<T> strategy)
    {
        return new MapCombinationKey<>(createIndexingFunction(required, excluded), strategy);
    }

    private static <T> Function<T, ?> createIndexingFunction(Requirements<T> required, Exclusions<T> excluded)
    {
        return in -> joinIfNotNull(
            DEFAULT_DELIMITER,
            required.stream().map(kc -> kc.supply(in)).toList(),
            excluded.stream().map(kc -> kc.supply(in)).collect(Collectors.toSet())
        );
    }

    private static <T> Function<T, ?> createQueryingFunction(Requirements<T> required)
    {
        return in -> joinIfNotNull(
            DEFAULT_DELIMITER,
            required.stream().map(kc -> kc.supply(in)).toList()
        );
    }

    @SafeVarargs
    public static <T> Requirements<T> requires(KeyComponent<T>... components)
    {
        var requirements = new Requirements<T>();
        requirements.addAll(Arrays.asList(components));
        return requirements;
    }

    @SafeVarargs
    public static <T> Exclusions<T> excludes(KeyComponent<T>... components)
    {
        var exclusions = new Exclusions<T>();
        exclusions.addAll(Arrays.asList(components));
        return exclusions;
    }

    private static String joinIfNotNull(String delimiter, String... args)
    {
        return joinIfNotNull(delimiter, Arrays.asList(args));
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

    private static final class Requirements<T> extends ArrayList<KeyComponent<T>> {}

    private static final class Exclusions<T> extends HashSet<KeyComponent<T>> {}

    public interface KeyComponent<T>
    {
        String supply(T input);
    }
}
