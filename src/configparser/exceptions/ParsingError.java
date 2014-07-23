package configparser.exceptions;

public abstract class ParsingError
{
    private final int lineNo;

    public ParsingError(int lineNo)
    {
        this.lineNo = lineNo;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParsingError other = (ParsingError) obj;
        if (lineNo != other.lineNo)
            return false;
        return true;
    }

    public int getLineNo()
    {
        return lineNo;
    }

    public abstract String getMessage();

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + lineNo;
        return result;
    }
}