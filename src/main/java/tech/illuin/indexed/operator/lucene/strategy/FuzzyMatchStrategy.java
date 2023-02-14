package tech.illuin.indexed.operator.lucene.strategy;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static tech.illuin.indexed.operator.lucene.LuceneIndexer.DEFAULT_FIELD;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class FuzzyMatchStrategy implements IndexStrategy
{
    private final Analyzer analyzer;
    private final int maxResults;
    private final String similarity;

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
        this.similarity = BigDecimal.valueOf(options.similarity).setScale(1, RoundingMode.HALF_UP).toString();
    }

    @Override
    public Analyzer getAnalyzer()
    {
        return this.analyzer;
    }

    @Override
    public Query createQuery(QueryParser parser, Object term) throws ParseException
    {
        return parser.parse(DEFAULT_FIELD + ":" + QueryParser.escape(Objects.toString(term)) + "~" + this.similarity);
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

    public static class Options
    {
        private int maxResults = DEFAULT_MAX_RESULTS;
        private float similarity = 0.4f;
        private Analyzer analyzer = new ShingleAnalyzerWrapper(new KeywordAnalyzer());

        private Options() {}

        public Options setMaxResults(int maxResults)
        {
            this.maxResults = maxResults;
            return this;
        }

        public Options setSimilarity(float similarity)
        {
            this.similarity = similarity;
            return this;
        }

        public Options setAnalyzer(Analyzer analyzer)
        {
            this.analyzer = analyzer;
            return this;
        }
    }
}
