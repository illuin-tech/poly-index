package tech.illuin.indexed.query;

import tech.illuin.indexed.key.Key;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record Query<T>(Key<T> key, Object value) {}
