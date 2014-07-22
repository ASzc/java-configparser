package configparser.exceptions;

import java.util.List;

public class InvalidLinesException extends IniParserException
{
    private static final long serialVersionUID = -4739216810114700091L;

    private static String createMessage(List<InvalidLine> nonFatalErrors)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Source contains parsing errors:");
        for (InvalidLine error : nonFatalErrors)
        {
            sb.append("\n\t");
            sb.append("[line ");
            sb.append(error.getLineNo());
            sb.append("]: ");
            sb.append(error.getLine());
        }

        return sb.toString();
    }

    public InvalidLinesException(List<InvalidLine> nonFatalErrors)
    {
        super(createMessage(nonFatalErrors));
    }
}
