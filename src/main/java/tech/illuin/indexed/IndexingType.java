package tech.illuin.indexed;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public enum IndexingType
{
    ALL, /* Will retain all indexed values (beware of memory usage, there is currently no eviction mechanism in place) */
    LAST, /* Will only retain the latest indexed value */
    FIRST, /* Will only retain the first indexed value */
}
