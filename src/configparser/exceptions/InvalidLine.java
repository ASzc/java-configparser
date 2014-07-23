package configparser.exceptions;

public class InvalidLine extends ParsingError
{
    private final String line;

    public InvalidLine(int lineNo, String line)
    {
        super(lineNo);
        this.line = line;
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
        InvalidLine other = (InvalidLine) obj;
        if (line == null)
        {
            if (other.line != null)
                return false;
        }
        else if (!line.equals(other.line))
            return false;
        return true;
    }

    public String getLine()
    {
        return line;
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Invalid: ");
        sb.append(getLine());

        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((line == null) ? 0 : line.hashCode());
        return result;
    }
}
