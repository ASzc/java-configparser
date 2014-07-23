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
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DuplicateSectionError other = (DuplicateSectionError) obj;
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

        sb.append("section ");
        sb.append(getSectionName());
        sb.append(" already exists");

        return sb.toString();
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
        result = prime * result + ((sectionName == null) ? 0 : sectionName.hashCode());
        return result;
    }
}
