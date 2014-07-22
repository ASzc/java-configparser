package configparser.exceptions;

import java.io.IOException;

public class IniParserException extends IOException
{
    private static final long serialVersionUID = -1241708876764785452L;

    public IniParserException()
    {
    }

    public IniParserException(String message)
    {
        super(message);
    }

    public IniParserException(Throwable cause)
    {
        super(cause);
    }

    public IniParserException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
