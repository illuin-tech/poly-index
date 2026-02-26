package tech.illuin.polyindex.exception;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class IndexClosingException extends RuntimeException
{
    public IndexClosingException(Throwable cause)
    {
        super(cause);
    }

    public IndexClosingException(String message)
    {
        super(message);
    }

    public IndexClosingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
