package configparser.exceptions;

public class MissingSectionHeaderException extends IniParserException
{
    private static final long serialVersionUID = -278117852489479514L;

    private static String createMessage(int lineNo, String line)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Source contains no section headers [line ");
        sb.append(lineNo);
        sb.append("]");

        return sb.toString();
    }

    public MissingSectionHeaderException(int lineNo, String line)
    {
        super(createMessage(lineNo, line));
    }

}
