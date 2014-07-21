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
import java.util.regex.Pattern;

public class Ini
{
    private static final Pattern sectionPattern = Pattern.compile(templateSectionPattern());
    
    private static final Pattern nonWhitespacePattern = Pattern.compile("\\S");

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
    private final List<String> inlineCommentPrefixes;

    private final Pattern optionPattern;

    public Ini()
    {
        this(false, null, null, null, false);
    }

    public Ini(boolean allowNoValue, List<String> delimiters, List<String> commentPrefixes,
            List<String> inlineCommentPrefixes, boolean allowDuplicates)
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
    }

    public void read(BufferedReader reader) throws IOException, IniParserException
    {
        IniParserException e = null;

        Map<String, String> currSection = null;
        String currSectionName = null;
        String currOptionName = null;
        int indentLevel = 0;
        String line = null;
        int lineNo = 0;
        
        while ((line = reader.readLine()) != null)
        {
            lineNo++;

            
        }
        
        if (e != null)
        {
            throw e;
        }
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