package configparser.exceptions;

import java.io.IOException;
import java.util.List;

public class IniParserException extends IOException
{
    private static final long serialVersionUID = -1241708876764785452L;

    private static String createMessage(List<ParsingError> parsingErrors)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Source contains parsing errors:");
        for (ParsingError error : parsingErrors)
        {
            sb.append("\n\t");
            sb.append("[line ");
            sb.append(error.getLineNo());
            sb.append("]: ");
            sb.append(error.getMessage());
        }

        return sb.toString();
    }

    private final List<ParsingError> parsingErrors;

    public IniParserException(List<ParsingError> parsingErrors)
    {
        super(createMessage(parsingErrors));
        this.parsingErrors = parsingErrors;
    }

    public List<ParsingError> getParsingErrors()
    {
        return parsingErrors;
    }
}
