package tech.illuin.indexed;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record Query<T>(Key<T> key, Object value) {}
