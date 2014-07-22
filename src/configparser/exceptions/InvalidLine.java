package configparser.exceptions;

public class InvalidLine
{
    private final String line;

    private final int lineNo;

    public InvalidLine(int lineNo, String line)
    {
        this.lineNo = lineNo;
        this.line = line;
    }

    public String getLine()
    {
        return line;
    }

    public int getLineNo()
    {
        return lineNo;
    }
}
