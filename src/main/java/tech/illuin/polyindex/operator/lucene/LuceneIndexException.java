package tech.illuin.polyindex.operator.lucene;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class LuceneIndexException extends RuntimeException
{
    public LuceneIndexException(Throwable cause)
    {
        super(cause);
    }

    public LuceneIndexException(String message)
    {
        super(message);
    }

    public LuceneIndexException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
