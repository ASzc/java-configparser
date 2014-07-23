package configparser;

/**
 * String methods that are more compatible with Python's
 */
public class StringUtil
{
    /**
     * Return a copy of a String with all whitespace characters removed from the end. Whitespace as defined by
     * {@link Character#isWhitespace(char)}
     */
    public static String rstrip(String s)
    {
        char[] val = s.toCharArray();

        int start = 0;
        int length = val.length;

        while ((start < length) && (Character.isWhitespace(val[length - 1])))
            length--;

        return s.substring(start, length);
    }

    /**
     * Return a copy of a String with all whitespace characters removed from the beginning and end. Whitespace as
     * defined by {@link Character#isWhitespace(char)}
     */
    public static String strip(String s)
    {
        char[] val = s.toCharArray();

        int start = 0;
        int length = val.length;

        while ((start < length) && (Character.isWhitespace(val[start])))
            start++;
        while ((start < length) && (Character.isWhitespace(val[length - 1])))
            length--;

        return s.substring(start, length);
    }
}
