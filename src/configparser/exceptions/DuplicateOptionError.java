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
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DuplicateOptionError other = (DuplicateOptionError) obj;
        if (optionName == null)
        {
            if (other.optionName != null)
                return false;
        }
        else if (!optionName.equals(other.optionName))
            return false;
        if (sectionName == null)
        {
            if (other.sectionName != null)
                return false;
        }
        else if (!sectionName.equals(other.sectionName))
            return false;
        return true;
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((optionName == null) ? 0 : optionName.hashCode());
        result = prime * result + ((sectionName == null) ? 0 : sectionName.hashCode());
        return result;
    }
}
