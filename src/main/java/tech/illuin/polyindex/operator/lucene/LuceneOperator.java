package tech.illuin.polyindex.operator.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.illuin.polyindex.Index;
import tech.illuin.polyindex.IndexType;
import tech.illuin.polyindex.exception.IndexClosingException;
import tech.illuin.polyindex.exception.UndefinedKeyException;
import tech.illuin.polyindex.key.Key;
import tech.illuin.polyindex.operator.IndexOperator;
import tech.illuin.polyindex.operator.lucene.strategy.ScoredDocument;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static tech.illuin.polyindex.operator.lucene.LuceneIndexer.UID_FIELD;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class LuceneOperator<T> implements IndexOperator<T>
{
    private final Map<LuceneKey<T>, LuceneIndexer> luceneIndexes;
    private final Map<UUID, T> dictionary;

    private static final Logger logger = LoggerFactory.getLogger(LuceneOperator.class);

    public LuceneOperator(Index<T> index, Supplier<Map<?, ?>> mapSupplier)
    {
        //noinspection unchecked
        this.luceneIndexes = (Map<LuceneKey<T>, LuceneIndexer>) mapSupplier.get();
        //noinspection unchecked
        this.dictionary = (Map<UUID, T>) mapSupplier.get();

        for (Key<T> key : index.keys())
        {
            if (key.type() != IndexType.LUCENE)
                continue;
            LuceneKey<T> luceneKey = (LuceneKey<T>) key;
            this.luceneIndexes.put(luceneKey, new LuceneIndexer(luceneKey.strategy()));
        }
        if (!this.luceneIndexes.isEmpty())
            logger.trace("Initialized lucene operator for index registry with {} LUCENE key(s)", this.luceneIndexes.size());
    }

    @Override
    public void push(Key<T> indexKey, Object key, T value)
    {
        LuceneKey<T> luceneKey = this.validateKey(indexKey);

        if (key == null)
            return;

        LuceneIndexer indexer = this.luceneIndexes.get(luceneKey);

        UUID uid = UUID.randomUUID();
        this.dictionary.put(uid, value);

        Document doc = new Document();
        doc.add(new StoredField(UID_FIELD, uid.toString()));
        for (Field field : luceneKey.strategy().createFields(key))
            doc.add(field);

        logger.trace("Pushing value for key \"{}\" to Lucene index {} using strategy {}", key, luceneKey.name(), luceneKey.strategy().getClass().getSimpleName());
        indexer.addDocument(doc);
    }

    @Override
    public boolean contains(Key<T> indexKey, Object key)
    {
        try {
            LuceneKey<T> luceneKey = this.validateKey(indexKey, true);

            LuceneIndexer indexer = this.luceneIndexes.get(luceneKey);
            Query query = luceneKey.strategy().createQuery(indexer.parser(), key);
            TopDocs top = indexer.searcher().search(query, 1);

            return top.totalHits.value > 0;
        }
        catch (ParseException | IOException e) {
            throw new LuceneIndexException(e);
        }
    }

    @Override
    public Optional<List<T>> get(Key<T> indexKey, Object key)
    {
        try {
            LuceneKey<T> luceneKey = this.validateKey(indexKey, true);

            LuceneIndexer indexer = this.luceneIndexes.get(luceneKey);
            Query query = luceneKey.strategy().createQuery(indexer.parser(), key);
            TopDocs top = indexer.searcher().search(query, luceneKey.strategy().maxResults());

            return Optional.of(this.getResults(luceneKey, indexer, top).toList());
        }
        catch (ParseException | IOException e) {
            throw new LuceneIndexException(e);
        }
    }

    @Override
    public Optional<List<T>> getAll(Key<T> indexKey)
    {
        try {
            LuceneKey<T> luceneKey = this.validateKey(indexKey, true);

            LuceneIndexer indexer = this.luceneIndexes.get(luceneKey);
            Query query = indexer.parser().parse("*:*");
            TopDocs all = indexer.searcher().search(query, this.count(luceneKey));

            return Optional.of(this.getResults(luceneKey, indexer, all).toList());
        }
        catch (ParseException | IOException e) {
            throw new LuceneIndexException(e);
        }
    }

    @Override
    public int count(Key<T> indexKey)
    {
        LuceneKey<T> luceneKey = this.validateKey(indexKey, true);

        return this.luceneIndexes.get(luceneKey).reader().numDocs();
    }

    @Override
    public Optional<List<T>> remove(Key<T> indexKey, Object key)
    {
        try {
            LuceneKey<T> luceneKey = this.validateKey(indexKey, true);

            LuceneIndexer indexer = this.luceneIndexes.get(luceneKey);
            Query query = luceneKey.strategy().createQuery(indexer.parser(), key);
            indexer.writer().deleteDocuments(query);

            return Optional.empty();
        }
        catch (ParseException | IOException e) {
            throw new LuceneIndexException(e);
        }
    }

    @Override
    public boolean isEmpty()
    {
        return this.luceneIndexes.values().stream()
            .map(idx -> idx.reader().numDocs())
            .max(Comparator.comparingInt(i -> i))
            .map(i -> i == 0)
            .orElse(true)
        ;
    }

    private ScoredDocument<T> mapToScored(StoredFields storedFields, ScoreDoc sc)
    {
        try {
            Document document = storedFields.document(sc.doc);
            UUID uid = UUID.fromString(document.get(UID_FIELD));
            T entry = this.dictionary.get(uid);
            return new ScoredDocument<>(sc, document, entry);
        }
        catch (IOException e) {
            throw new LuceneIndexException(e);
        }
    }

    private Stream<T> getResults(LuceneKey<T> index, LuceneIndexer indexer, TopDocs docs)
    {
        try {
            StoredFields storedFields = indexer.reader().storedFields();
            var stream = Stream.of(docs.scoreDocs)
                .map(sc -> this.mapToScored(storedFields, sc))
            ;

            return index.strategy().postProcess(stream).map(ScoredDocument::entry);
        }
        catch (IOException e) {
            throw new LuceneIndexException(e);
        }
    }

    private LuceneKey<T> validateKey(Key<T> key)
    {
        return this.validateKey(key, false);
    }

    private LuceneKey<T> validateKey(Key<T> key, boolean checkRegistration)
    {
        if (!(key instanceof LuceneKey<T> luceneKey))
            throw new IllegalArgumentException("An unexpected index key of type " + key.getClass() + " was supplied while a LuceneKey is expected");
        if (checkRegistration && !this.luceneIndexes.containsKey(key))
            throw new UndefinedKeyException("The provided key is not part of this store's indexes");
        return luceneKey;
    }

    @Override
    public void close() throws IndexClosingException
    {
        try {
            if (this.luceneIndexes.isEmpty())
                return;

            logger.trace("Closing down lucene operator with {} indexer(s)", this.luceneIndexes.size());
            for (LuceneIndexer indexer : this.luceneIndexes.values())
                indexer.close();
        }
        catch (IOException e) {
            throw new IndexClosingException(e);
        }
    }
}
