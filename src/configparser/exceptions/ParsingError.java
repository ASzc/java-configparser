package configparser.exceptions;

public abstract class ParsingError
{
    private final int lineNo;

    public ParsingError(int lineNo)
    {
        this.lineNo = lineNo;
    }

    public int getLineNo()
    {
        return lineNo;
    }

    public abstract String getMessage();
}