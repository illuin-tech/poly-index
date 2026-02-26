package tech.illuin.polyindex.exception;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class UndefinedKeyException extends RuntimeException
{
    public UndefinedKeyException(String message)
    {
        super(message);
    }

    public UndefinedKeyException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
