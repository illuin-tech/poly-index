package tech.illuin.polyindex.operator.lucene.strategy;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MultiFieldMatchStrategy<T> implements LuceneIndexStrategy
{
    private final Analyzer analyzer;
    private final QueryParser parser;
    private final int maxResults;
    private final FieldFunction<T> fieldFunction;
    private final QueryFunction<T> queryFunction;

    public MultiFieldMatchStrategy(FieldFunction<T> fieldFunction, QueryFunction<T> queryFunction)
    {
        this(fieldFunction, queryFunction, opts -> {});
    }

    public MultiFieldMatchStrategy(
        FieldFunction<T> fieldFunction,
        QueryFunction<T> queryFunction,
        Consumer<Options> optionBuilder
    ) {
        this.fieldFunction = fieldFunction;
        this.queryFunction = queryFunction;
        var options = new Options();
        optionBuilder.accept(options);
        this.analyzer = options.analyzer;
        this.maxResults = options.maxResults;
        this.parser = new MultiFieldQueryParser(new String[0], this.analyzer);
    }

    @Override
    public Analyzer getAnalyzer()
    {
        return this.analyzer;
    }

    @Override
    public QueryParser getParser()
    {
        return this.parser;
    }

    @Override
    public Query createQuery(QueryParser parser, Object arg) throws ParseException
    {
        //noinspection unchecked
        return this.queryFunction.createQuery(parser, (T) arg);
    }

    @Override
    public List<Field> createFields(Object key)
    {
        //noinspection unchecked
        return this.fieldFunction.createFields((T) key);
    }

    @Override
    public int maxResults()
    {
        return this.maxResults;
    }

    @Override
    public <D> Stream<ScoredDocument<D>> postProcess(Stream<ScoredDocument<D>> stream)
    {
        return stream;
    }

    public interface FieldFunction<T>
    {
        List<Field> createFields(T object);
    }

    public interface QueryFunction<T>
    {
        Query createQuery(QueryParser parser, T object) throws ParseException;
    }

    public static final class Options
    {
        private int maxResults = DEFAULT_MAX_RESULTS;
        private Analyzer analyzer = new StandardAnalyzer();

        private Options() {}

        public Options setMaxResults(int maxResults)
        {
            this.maxResults = maxResults;
            return this;
        }

        public Options setAnalyzer(Analyzer analyzer)
        {
            this.analyzer = analyzer;
            return this;
        }
    }
}
