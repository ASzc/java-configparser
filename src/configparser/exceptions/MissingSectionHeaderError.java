package configparser.exceptions;

public class MissingSectionHeaderError extends InvalidLine
{
    public MissingSectionHeaderError(int lineNo, String line)
    {
        super(lineNo, line);
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("option ");
        sb.append(getLine());
        sb.append(" is declared outside of a section header");

        return sb.toString();
    }
}
