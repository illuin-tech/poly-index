package tech.illuin.indexed.operator.lucene.strategy;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static tech.illuin.indexed.operator.lucene.LuceneIndexer.DEFAULT_FIELD;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class PartialMatchStrategy implements IndexStrategy
{
    private final int maxResults;

    public PartialMatchStrategy()
    {
        this(DEFAULT_MAX_RESULTS);
    }

    public PartialMatchStrategy(int maxResults)
    {
        this.maxResults = maxResults;
    }

    @Override
    public String createQuery(Object term)
    {
        return DEFAULT_FIELD + ":" + QueryParser.escape(Objects.toString(term)) + "*";
    }

    @Override
    public List<Field> createFields(Object term)
    {
        return List.of(
            new TextField(DEFAULT_FIELD, Objects.toString(term), Field.Store.YES)
        );
    }

    @Override
    public int maxResults()
    {
        return this.maxResults;
    }

    @Override
    public <T> Stream<ScoredDocument<T>> postProcess(Stream<ScoredDocument<T>> stream)
    {
        Comparator<ScoredDocument<T>> scoreComparator = (o1, o2) -> Float.compare(o2.scoreDoc().score, o1.scoreDoc().score);
        Comparator<ScoredDocument<T>> lengthComparator = Comparator.comparingInt(o -> o.document().get(DEFAULT_FIELD).length());
        return stream.sorted(scoreComparator.thenComparing(lengthComparator));
    }
}
