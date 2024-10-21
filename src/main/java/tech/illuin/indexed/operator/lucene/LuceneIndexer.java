package tech.illuin.indexed.operator.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import tech.illuin.indexed.operator.lucene.strategy.LuceneIndexStrategy;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class LuceneIndexer implements Closeable
{
    private final Directory directory;
    private final IndexWriter writer;
    private final QueryParser parser;
    private DirectoryReader reader;

    public static final String UID_FIELD = "uid";
    public static final String DEFAULT_FIELD = "key";

    public LuceneIndexer(LuceneIndexStrategy strategy)
    {
        try {
            Analyzer analyzer = strategy.getAnalyzer();
            this.directory = new ByteBuffersDirectory();
            this.writer = new IndexWriter(this.directory, new IndexWriterConfig(analyzer));
            this.reader = DirectoryReader.open(this.writer);
            this.parser = strategy.getParser();
        }
        catch (IOException e) {
            throw new LuceneIndexException("An error occurred while attempting to initialize a LuceneIndexer", e);
        }
    }

    public LuceneIndexer addDocument(Document document)
    {
        try {
            this.writer.addDocument(document);
            this.writer.commit();
            return this;
        }
        catch (IOException e) {
            throw new LuceneIndexException(e);
        }
    }

    public synchronized IndexReader reader()
    {
        try {
            DirectoryReader reopened = DirectoryReader.openIfChanged(this.reader);
            if (reopened != null)
                this.reader = reopened;
            return this.reader;
        }
        catch (IOException e) {
            throw new LuceneIndexException(e);
        }
    }

    public IndexWriter writer()
    {
        return this.writer;
    }

    public IndexSearcher searcher()
    {
        return new IndexSearcher(this.reader());
    }

    public QueryParser parser()
    {
        return this.parser;
    }

    @Override
    public void close() throws IOException
    {
        this.writer.close();
        this.directory.close();
    }
}
