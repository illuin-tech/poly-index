package tech.illuin.polyindex.operator.lucene.strategy;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public record ScoredDocument<T>(
    ScoreDoc scoreDoc,
    Document document,
    T entry
) {}
