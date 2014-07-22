package configparser.exceptions;

public class InvalidLine extends ParsingError
{
    private final String line;

    public InvalidLine(int lineNo, String line)
    {
        super(lineNo);
        this.line = line;
    }

    public String getLine()
    {
        return line;
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Invalid: ");
        sb.append(getLine());

        return sb.toString();
    }
}
