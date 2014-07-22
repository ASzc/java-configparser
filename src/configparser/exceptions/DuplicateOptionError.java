package configparser.exceptions;

public class DuplicateOptionError extends ParsingError
{
    private final String optionName;
    private final String sectionName;

    public DuplicateOptionError(int lineNo, String sectionName, String optionName)
    {
        super(lineNo);
        this.sectionName = sectionName;
        this.optionName = optionName;
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("option ");
        sb.append(getOptionName());
        sb.append(" in section ");
        sb.append(getSectionName());
        sb.append(" already exists");

        return sb.toString();
    }

    public String getOptionName()
    {
        return optionName;
    }

    public String getSectionName()
    {
        return sectionName;
    }
}
