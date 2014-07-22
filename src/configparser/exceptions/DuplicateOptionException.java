package configparser.exceptions;

public class DuplicateOptionException extends IniParserException
{
    private static final long serialVersionUID = -8536045030665028704L;

    private static String createMessage(String sectionName, String optionName, int lineNo)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Source contains duplicate option [line ");
        sb.append(lineNo);
        sb.append("]: option ");
        sb.append(optionName);
        sb.append(" in section ");
        sb.append(sectionName);
        sb.append(" already exists");
        
        return sb.toString();
    }

    public DuplicateOptionException(String currSectionName, String currOptionName, int lineNo)
    {
        super(createMessage(currSectionName, currOptionName, lineNo));
    }
}
