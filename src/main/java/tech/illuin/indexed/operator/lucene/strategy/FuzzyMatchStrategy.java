package tech.illuin.indexed.operator.lucene.strategy;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static tech.illuin.indexed.operator.lucene.LuceneIndexer.DEFAULT_FIELD;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class FuzzyMatchStrategy implements LuceneIndexStrategy
{
    private final Analyzer analyzer;
    private final int maxResults;
    private final int maxDistance;

    public FuzzyMatchStrategy()
    {
        this(opts -> {});
    }

    public FuzzyMatchStrategy(Consumer<Options> optionBuilder)
    {
        var options = new Options();
        optionBuilder.accept(options);
        this.analyzer = options.analyzer;
        this.maxResults = options.maxResults;
        this.maxDistance = options.maxDistance;
    }

    @Override
    public Analyzer getAnalyzer()
    {
        return this.analyzer;
    }

    @Override
    public Query createQuery(QueryParser parser, Object term)
    {
        return new FuzzyQuery(new Term(DEFAULT_FIELD, Objects.toString(term)), this.maxDistance);
    }

    @Override
    public List<Field> createFields(Object term)
    {
        return List.of(
            new StringField(DEFAULT_FIELD, Objects.toString(term), Field.Store.YES)
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

    public static final class Options
    {
        private int maxResults = DEFAULT_MAX_RESULTS;
        private int maxDistance = 2;
        private Analyzer analyzer = new ShingleAnalyzerWrapper(new KeywordAnalyzer());

        private Options() {}

        public Options setMaxResults(int maxResults)
        {
            this.maxResults = maxResults;
            return this;
        }

        public Options setMaxDistance(int maxDistance)
        {
            this.maxDistance = maxDistance;
            return this;
        }

        public Options setAnalyzer(Analyzer analyzer)
        {
            this.analyzer = analyzer;
            return this;
        }
    }
}
