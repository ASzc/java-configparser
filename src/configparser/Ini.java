package configparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import configparser.exceptions.DuplicateOptionError;
import configparser.exceptions.DuplicateSectionError;
import configparser.exceptions.IniParserException;
import configparser.exceptions.InvalidLine;
import configparser.exceptions.MissingSectionHeaderError;
import configparser.exceptions.ParsingError;

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

    private boolean allowDuplicates;
    private boolean allowNoValue;
    private List<String> commentPrefixes;
    private List<String> delimiters;
    private boolean emptyLinesInValues;
    private List<String> inlineCommentPrefixes;
    private Pattern optionPattern;

    private final Map<String, Map<String, String>> sections;

    private boolean spaceAroundDelimiters;

    public Ini()
    {
        allowDuplicates = false;

        allowNoValue = false;

        commentPrefixes = new ArrayList<>(2);
        commentPrefixes.add("#");
        commentPrefixes.add(";");

        delimiters = new ArrayList<>(2);
        delimiters.add("=");
        delimiters.add(":");

        emptyLinesInValues = true;

        inlineCommentPrefixes = new ArrayList<>(0);

        compileOptionPattern();

        sections = new LinkedHashMap<>();

        spaceAroundDelimiters = true;
    }

    private void compileOptionPattern()
    {
        optionPattern = Pattern.compile(templateOptionPattern(delimiters, allowNoValue));
    }

    public List<String> getCommentPrefixes()
    {
        return commentPrefixes;
    }

    public List<String> getDelimiters()
    {
        return delimiters;
    }

    public List<String> getInlineCommentPrefixes()
    {
        return inlineCommentPrefixes;
    }

    public Map<String, Map<String, String>> getSections()
    {
        return sections;
    }

    public boolean isAllowDuplicates()
    {
        return allowDuplicates;
    }

    public boolean isAllowNoValue()
    {
        return allowNoValue;
    }

    public boolean isEmptyLinesInValues()
    {
        return emptyLinesInValues;
    }

    public boolean isSpaceAroundDelimiters()
    {
        return spaceAroundDelimiters;
    }

    public Ini read(BufferedReader reader) throws IOException, IniParserException
    {
        List<ParsingError> parsingErrors = new LinkedList<>();
        Map<String, Map<String, List<String>>> unjoinedSections = new LinkedHashMap<>();
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

            if (commentStart != 0)
            {
                // Full line comment?
                for (String prefix : commentPrefixes)
                {
                    if (StringUtil.strip(line).startsWith(prefix))
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
            value = StringUtil.strip(value);

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

                    Matcher sectionMatcher = sectionPattern.matcher(value);
                    // Section header
                    if (sectionMatcher.matches())
                    {
                        currSectionName = sectionMatcher.group("header");
                        if (unjoinedSections.containsKey(currSectionName))
                        {
                            if (!allowDuplicates)
                            {
                                parsingErrors.add(new DuplicateSectionError(lineNo, currSectionName));
                            }
                            else
                            {
                                currSection = unjoinedSections.get(currSectionName);
                            }
                        }
                        else
                        {
                            currSection = new LinkedHashMap<>();
                            unjoinedSections.put(currSectionName, currSection);
                        }
                        // So sections can't start with a continuation line
                        currOptionName = null;
                    }
                    // No section header in file
                    else if (currSection == null)
                    {
                        parsingErrors.add(new MissingSectionHeaderError(lineNo, line));
                    }
                    // Option header
                    else
                    {
                        Matcher optionMatcher = optionPattern.matcher(value);
                        if (optionMatcher.matches())
                        {
                            currOptionName = optionMatcher.group("option");
                            String optionValue = optionMatcher.group("value");
                            if (currOptionName == null || currOptionName.length() == 0)
                            {
                                parsingErrors.add(new InvalidLine(lineNo, line));
                            }
                            currOptionName = StringUtil.rstrip(currOptionName).toLowerCase();
                            if (!allowDuplicates && unjoinedSections.get(currSectionName).containsKey(currOptionName))
                            {
                                parsingErrors.add(new DuplicateOptionError(lineNo, currSectionName, currOptionName));
                            }
                            else
                            {
                                LinkedList<String> valueList = new LinkedList<>();
                                if (optionValue != null)
                                {
                                    optionValue = StringUtil.rstrip(optionValue);
                                    valueList.add(optionValue);
                                }
                                currSection.put(currOptionName, valueList);
                            }
                        }
                        else
                        {
                            parsingErrors.add(new InvalidLine(lineNo, line));
                        }
                    }
                }
            }
        }

        if (parsingErrors.size() > 0)
            throw new IniParserException(parsingErrors);

        // Join multi line values
        for (Entry<String, Map<String, List<String>>> unjoinedSectionEntry : unjoinedSections.entrySet())
        {
            String unjoinedSectionName = unjoinedSectionEntry.getKey();
            Map<String, List<String>> unjoinedSectionOptions = unjoinedSectionEntry.getValue();

            Map<String, String> sectionOptions = new LinkedHashMap<>();

            for (Entry<String, List<String>> unjoinedOptionValueEntry : unjoinedSectionOptions.entrySet())
            {
                String unjoinedOptionName = unjoinedOptionValueEntry.getKey();
                List<String> unjoinedOptionValue = unjoinedOptionValueEntry.getValue();

                String optionValue;

                if (unjoinedOptionValue.get(0) != null)
                {
                    // Remove trailing whitespace lines
                    ListIterator<String> iter = unjoinedOptionValue.listIterator(unjoinedOptionValue.size());
                    while (iter.hasPrevious())
                        if (StringUtil.strip(iter.previous()).isEmpty())
                            iter.remove();

                    // Join lines with newline character
                    StringBuilder optionValueBuilder = new StringBuilder();
                    String prefix = "";
                    for (String valueLine : unjoinedOptionValue)
                    {
                        optionValueBuilder.append(prefix);
                        prefix = "\n";
                        optionValueBuilder.append(valueLine);
                    }
                    optionValue = optionValueBuilder.toString();
                }
                else
                {
                    optionValue = null;
                }

                sectionOptions.put(unjoinedOptionName, optionValue);
            }

            sections.put(unjoinedSectionName, sectionOptions);
        }
        return this;
    }

    public Ini read(Path iniPath) throws IOException, IniParserException
    {
        read(iniPath, StandardCharsets.UTF_8);
        return this;
    }

    public Ini read(Path iniPath, Charset charset) throws IOException, IniParserException
    {
        try (BufferedReader reader = Files.newBufferedReader(iniPath, charset))
        {
            read(reader);
        }
        return this;
    }

    public Ini setAllowDuplicates(boolean allowDuplicates)
    {
        this.allowDuplicates = allowDuplicates;
        return this;
    }

    public Ini setAllowNoValue(boolean allowNoValue)
    {
        this.allowNoValue = allowNoValue;

        compileOptionPattern();
        return this;
    }

    public Ini setCommentPrefixes(List<String> commentPrefixes)
    {
        this.commentPrefixes = commentPrefixes;
        return this;
    }

    public Ini setDelimiters(List<String> delimiters)
    {
        this.delimiters = delimiters;

        compileOptionPattern();
        return this;
    }

    public Ini setEmptyLinesInValues(boolean emptyLinesInValues)
    {
        this.emptyLinesInValues = emptyLinesInValues;
        return this;
    }

    public Ini setInlineCommentPrefixes(List<String> inlineCommentPrefixes)
    {
        this.inlineCommentPrefixes = inlineCommentPrefixes;
        return this;
    }

    public Ini setSpaceAroundDelimiters(boolean spaceAroundDelimiters)
    {
        this.spaceAroundDelimiters = spaceAroundDelimiters;
        return this;
    }

    public Ini write(BufferedWriter writer) throws IOException
    {
        // Create option/value delimiter string using first in configured delimiters
        StringBuilder sb = new StringBuilder();
        if (spaceAroundDelimiters)
            sb.append(" ");
        sb.append(delimiters.get(0));
        if (spaceAroundDelimiters)
            sb.append(" ");
        String delimiter = sb.toString();

        // Write out each section
        for (Entry<String, Map<String, String>> sectionEntry : sections.entrySet())
        {
            String sectionName = sectionEntry.getKey();
            Map<String, String> sectionOptions = sectionEntry.getValue();

            // Section Header (ex: [mysection])
            writer.append("[");
            writer.append(sectionName);
            writer.append("]");
            writer.newLine();

            // Write out each option/value pair
            for (Entry<String, String> optionEntry : sectionOptions.entrySet())
            {
                String option = optionEntry.getKey();
                String value = optionEntry.getValue();

                // Option Header (ex: key = value)
                writer.append(option);
                if (value == null && allowNoValue)
                {
                    // Append nothing after the key
                }
                else
                {
                    writer.append(delimiter);
                    if (value != null)
                    {
                        writer.append(value.replace("\n", System.lineSeparator() + "\t"));
                    }
                    else
                    {
                        writer.append(value);
                    }
                }
                writer.newLine();
            }

            writer.newLine();
        }
        return this;
    }

    public Ini write(Path iniPath) throws IOException
    {
        write(iniPath, StandardCharsets.UTF_8);
        return this;
    }

    public Ini write(Path iniPath, Charset charset) throws IOException
    {
        try (BufferedWriter writer = Files.newBufferedWriter(iniPath, charset))
        {
            write(writer);
        }
        return this;
    }
}