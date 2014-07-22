package configparser.exceptions;

public class DuplicateSectionException extends IniParserException
{
    private static final long serialVersionUID = -5887841423192964270L;

    private static String createMessage(String sectionName, int lineNo)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Source contains duplicate section [line ");
        sb.append(lineNo);
        sb.append("]: section ");
        sb.append(sectionName);
        sb.append(" already exists");
        
        return sb.toString();
    }

    public DuplicateSectionException(String currSectionName, int lineNo)
    {
        super(createMessage(currSectionName, lineNo));
    }
}
