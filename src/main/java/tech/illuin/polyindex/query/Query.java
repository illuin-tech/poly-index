package tech.illuin.polyindex.query;

import tech.illuin.polyindex.key.Key;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record Query<T>(Key<T> key, Object value) {}
