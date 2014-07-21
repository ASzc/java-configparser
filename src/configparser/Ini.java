package configparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ini
{
    private static final Pattern nonWhitespacePattern = Pattern.compile("\\S");

    private static final Pattern sectionPattern = Pattern.compile(templateSectionPattern());

    private static String templateOptionPattern(List<String> delimiters, boolean allowNoValue)
    {
        // Join delimiters with | character
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for (String delimiter : delimiters)
        {
            sb.append(prefix);
            prefix = "|";
            sb.append(Pattern.quote(delimiter));
        }
        String delimiterRegEx = sb.toString();

        // Create the option pattern
        sb = new StringBuilder();

        // Option name: any characters
        sb.append("(?<option>.*?)");

        // Zero or more whitespace
        sb.append("\\s*");

        // Open optional value group
        if (allowNoValue)
            sb.append("(?:");

        // Delimiter: one option in delimiterRegEx
        sb.append("(?<vi>");
        sb.append(delimiterRegEx);
        sb.append(")");

        // Zero or more whitespace
        sb.append("\\s*");

        // Value: all remaining characters
        sb.append("(?<value>.*)");

        // Close optional value group
        if (allowNoValue)
            sb.append(")?");

        // End of line
        sb.append("$");

        return sb.toString();
    }

    private static String templateSectionPattern()
    {
        StringBuilder sb = new StringBuilder();

        // Literal [
        sb.append("\\[");

        // Header: one or more characters except literal ]
        sb.append("(?<header>[^]]+)");

        // Literal ]
        sb.append("\\]");

        return sb.toString();
    }

    private final boolean allowDuplicates;
    private final List<String> commentPrefixes;
    private final boolean emptyLinesInValues;

    private final List<String> inlineCommentPrefixes;

    private final Pattern optionPattern;

    public Ini()
    {
        this(false, null, null, null, false, true);
    }

    public Ini(boolean allowNoValue, List<String> delimiters, List<String> commentPrefixes,
            List<String> inlineCommentPrefixes, boolean allowDuplicates, boolean emptyLinesInValues)
    {
        if (delimiters == null)
        {
            delimiters = new ArrayList<>(2);
            delimiters.add("=");
            delimiters.add(":");
        }

        optionPattern = Pattern.compile(templateOptionPattern(delimiters, allowNoValue));

        if (commentPrefixes == null)
        {
            commentPrefixes = new ArrayList<>(2);
            commentPrefixes.add("#");
            commentPrefixes.add(";");
        }

        this.commentPrefixes = commentPrefixes;

        if (inlineCommentPrefixes == null)
        {
            inlineCommentPrefixes = new ArrayList<>(0);
        }

        this.inlineCommentPrefixes = inlineCommentPrefixes;

        this.allowDuplicates = allowDuplicates;

        this.emptyLinesInValues = emptyLinesInValues;
    }

    public void read(BufferedReader reader) throws IOException, IniParserException
    {
        IniParserException e = null;

        Map<String, List<String>> currSection = null;
        String currSectionName = null;
        String currOptionName = null;
        int indentLevel = 0;
        String line = null;
        int lineNo = 0;

        while ((line = reader.readLine()) != null)
        {
            lineNo++;

            // Strip comments
            int commentStart = -1;

            // If there are any to search for, find the earliest instance of an inline comment character with a
            // whitespace character before it
            if (inlineCommentPrefixes.size() > 0)
            {
                int earliestIndex = Integer.MAX_VALUE;
                for (String prefix : inlineCommentPrefixes)
                {
                    int index = line.indexOf(prefix);
                    if (index == 0)
                    {
                        earliestIndex = 0;
                        break;
                    }
                    else if (index > 0 && Character.isWhitespace(line.charAt(index - 1)))
                        earliestIndex = Math.min(earliestIndex, index);
                }
                commentStart = earliestIndex;
            }

            if (commentStart > 0)
            {
                // Full line comment?
                for (String prefix : commentPrefixes)
                {
                    if (line.trim().startsWith(prefix))
                    {
                        commentStart = 0;
                        break;
                    }
                }
            }

            // Get the trimmed non-comment substring, if applicable
            String value;
            if (commentStart != -1)
                value = line.substring(0, commentStart);
            else
                value = line;
            value = value.trim();

            if (value.length() == 0)
            {
                if (emptyLinesInValues)
                {
                    // For ongoing option values, add an empty line, but only if there was no comment on this line
                    if (commentStart == -1 && currSection != null && currOptionName != null
                            && currSection.containsKey(currOptionName))
                    {
                        currSection.get(currOptionName).add("");
                    }
                }
                else
                {
                    indentLevel = Integer.MAX_VALUE;
                }
            }
            else
            {
                // Find index of first non-whitespace character in the raw line (not value)
                Matcher nonWhitespaceMatcher = nonWhitespacePattern.matcher(line);
                int firstNonWhitespace = -1;
                if (nonWhitespaceMatcher.find())
                    firstNonWhitespace = nonWhitespaceMatcher.start();
                // This is the indent level, otherwise it is zero
                int currIndentLevel = Math.max(firstNonWhitespace, 0);

                // Continuation line
                if (currSection != null && currOptionName != null && currIndentLevel > indentLevel)
                {
                    currSection.get(currOptionName).add(value);
                }
                // Section/option header
                else
                {
                    indentLevel = currIndentLevel;

                    // Section header
                    
                    // No section header in file
                    
                    // Option header/line
                }
            }
        }

        // Throw any parsing errors all at once
        if (e != null)
        {
            throw e;
        }
        
        // TODO join multi line values
    }

    public void read(Path iniPath) throws IOException, IniParserException
    {
        read(iniPath, StandardCharsets.UTF_8);
    }

    public void read(Path iniPath, Charset charset) throws IOException, IniParserException
    {
        try (BufferedReader reader = Files.newBufferedReader(iniPath, charset))
        {
            read(reader);
        }
    }
}