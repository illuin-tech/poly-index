package tech.illuin.polyindex.operator.lucene.strategy;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public interface LuceneIndexStrategy
{
    int DEFAULT_MAX_RESULTS = 25;

    Analyzer getAnalyzer();

    QueryParser getParser();

    Query createQuery(QueryParser parser, Object term) throws ParseException;

    List<Field> createFields(Object key);

    int maxResults();

    <T> Stream<ScoredDocument<T>> postProcess(Stream<ScoredDocument<T>> stream);
}
