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
        int end = val.length - 1;

        while ((start < end) && (Character.isWhitespace(val[end])))
            end--;

        return s.substring(start, end + 1);
    }

    /**
     * Return a copy of a String with all whitespace characters removed from the beginning and end. Whitespace as
     * defined by {@link Character#isWhitespace(char)}
     */
    public static String strip(String s)
    {
        char[] val = s.toCharArray();

        int start = 0;
        int end = val.length - 1;

        while ((start < end) && (Character.isWhitespace(val[start])))
            start++;
        while ((start < end) && (Character.isWhitespace(val[end])))
            end--;

        return s.substring(start, end + 1);
    }
}
