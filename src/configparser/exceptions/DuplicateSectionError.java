package configparser.exceptions;

public class DuplicateSectionError extends ParsingError
{
    private final String sectionName;

    public DuplicateSectionError(int lineNo, String sectionName)
    {
        super(lineNo);
        this.sectionName = sectionName;
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("section ");
        sb.append(getSectionName());
        sb.append(" already exists");

        return sb.toString();
    }

    public String getSectionName()
    {
        return sectionName;
    }
}
