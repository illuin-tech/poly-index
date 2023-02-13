package tech.illuin.indexed.operator.lucene.strategy;

import org.apache.lucene.document.Field;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface IndexStrategy
{
    int DEFAULT_MAX_RESULTS = 25;

    String createQuery(Object term);

    List<Field> createFields(Object key);

    int maxResults();

    <T> Stream<ScoredDocument<T>> postProcess(Stream<ScoredDocument<T>> stream);
}
