/**
 * Copyright 2014, 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.szc.configparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.szc.configparser.exceptions.DuplicateOptionError;
import ca.szc.configparser.exceptions.DuplicateSectionError;
import ca.szc.configparser.exceptions.IniParserException;
import ca.szc.configparser.exceptions.InterpolationDepthError;
import ca.szc.configparser.exceptions.InterpolationMissingOptionError;
import ca.szc.configparser.exceptions.InterpolationSyntaxError;
import ca.szc.configparser.exceptions.InvalidLine;
import ca.szc.configparser.exceptions.MissingSectionHeaderError;
import ca.szc.configparser.exceptions.NoOptionError;
import ca.szc.configparser.exceptions.NoSectionError;
import ca.szc.configparser.exceptions.ParsingError;


/**
 * A Python-compatible Java INI parser
 */
public class Ini
{
    private static final Pattern nonWhitespacePattern = Pattern.compile("\\S");

    private static final Pattern sectionPattern = Pattern.compile(templateSectionPattern());

    private static final Pattern interpolationPattern = Pattern.compile("\\$\\{([^}]+)\\}");

    private static final int MAX_INTERPOLATION_DEPTH = 10;

    /**
     * Create the regular expression source for the {@link #optionPattern}
     */
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

    /**
     * Create the regular expression source for the {@link #sectionPattern}
     */
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
    private boolean allowInterpolation;
    private boolean allowNoValue;
    private List<String> commentPrefixes;
    private List<String> delimiters;
    private boolean emptyLinesInValues;
    private List<String> inlineCommentPrefixes;
    /** Map section:option to line number for use in error reporting. */
    private Map<String, Integer> lineNumberMap;
    private Pattern optionPattern;
    List<ParsingError> parsingErrors = new LinkedList<>();

    private final Map<String, Map<String, String>> sections;

    private boolean spaceAroundDelimiters;

    private Map<String, String> rawValues;

    /**
     * Creates an INI parser with the default configuration
     */
    public Ini()
    {
        allowDuplicates = false;

        allowInterpolation = true;

        allowNoValue = false;

        commentPrefixes = new ArrayList<>(2);
        commentPrefixes.add("#");
        commentPrefixes.add(";");

        delimiters = new ArrayList<>(2);
        delimiters.add("=");
        delimiters.add(":");

        emptyLinesInValues = true;

        inlineCommentPrefixes = new ArrayList<>(0);

        lineNumberMap = new HashMap<String, Integer>();

        compileOptionPattern();

        sections = new LinkedHashMap<>();

        spaceAroundDelimiters = true;

        rawValues = new HashMap<String, String>();
    }

    /**
     * Must be called after updating attributes that {@link #templateOptionPattern(List, boolean)} depends on.
     */
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

    public String getValue (String sectionName, String optionName) throws NoSectionError, NoOptionError
    {
        Map<String, String> section = sections.get (sectionName);
        if (section == null)
        {
            throw new NoSectionError (sectionName);
        }

        if (!section.containsKey (optionName.toLowerCase()))
        {
            throw new NoOptionError (sectionName, optionName);
        }

        return section.get (optionName.toLowerCase());
    }

    public String getValue (String sectionName, String optionName, String fallback) throws NoSectionError, NoOptionError
    {
        String value;
        try
        {
            value = getValue(sectionName, optionName);
        }
        catch (NoOptionError ex)
        {
            if (fallback == null)
            {
                throw ex;
            }
            else
            {
                return fallback;
            }
        }
        return value;
    }

    private void interpolate() throws IniParserException
    {
        for (String sectionName : sections.keySet())
        {
            Map<String, String> options = sections.get(sectionName);
            for (String optionName : options.keySet())
            {
                ArrayList<String> L = new ArrayList<String>();
                String rawValue = options.get(optionName);
                ParsingError pe = interpolate (sectionName, optionName, L, rawValue, 1);

                if (pe == null)
                {
                    StringBuffer sb = new StringBuffer();
                    for (String component : L)
                    {
                        sb.append(component);
                    }
                    options.put(optionName, sb.toString());
                    rawValues.put (sectionName + ":" + optionName.toLowerCase(), rawValue);
                }
                else
                {
                    parsingErrors.add(pe);
                }
            }
        }
    }

    private ParsingError interpolate (String section, String option, List<String> accum, String rest, int depth)
    {
        String rawval = "";
        int lineNo = lineNumberMap.get (section + ":" + option);

        try
        {
            rawval = getValue(section, option, rest);
        }
        catch (Exception ex)
        {
            // getValue will not throw an exception as section and option are both valid at this point.
        }

        if (depth > MAX_INTERPOLATION_DEPTH)
        {
            return new InterpolationDepthError (lineNo, option, section, rawval);
        }

        while (rest != null && rest.length() > 0)
        {
            int p = rest.indexOf('$');
            if (p < 0)
            {
                accum.add(rest);
                return null;
            }
            if (p > 0)
            {
                accum.add(rest.substring(0, p));
                rest = rest.substring(p);
            }

            char c = rest.charAt(1);
            if (c == '$')
            {
                accum.add("" + c);
                rest = rest.substring(2);
            }
            else if (c == '{')
            {
                Matcher m = interpolationPattern.matcher(rest);
                if (!m.find())
                {
                    return new InterpolationSyntaxError (lineNo, option, section, "bad interpolation variable reference " + rest);
                }
                String[] path = m.group(1).split(":");
                rest = rest.substring(m.end());
                String sect = section;
                String opt = option;
                String value;
                try
                {
                    if (path.length == 1)
                    {
                        opt = path[0];
                        value = getValue(section, opt);
                    }
                    else if (path.length == 2)
                    {
                        sect = path[0];
                        opt = path[1];
                        value = getValue(sect, opt);
                    }
                    else
                    {
                        return new InterpolationSyntaxError (lineNo, option, section, "More that one ':' found: " + rest);
                    }
                }
                catch (Exception ex)
                {
                    return new InterpolationMissingOptionError (lineNo, option, section, rawval, m.group (1));
                }

                if (value.indexOf("$") > 0)
                {
                    this.interpolate (sect, opt, accum, value, depth + 1);
                }
                else
                {
                    accum.add(value);
                }
            }
            else
            {
                return new InterpolationSyntaxError (lineNo, option, section, "'$' must be followed by '$' or '{', found: " + rest);
            }
        }
        return null;
    }

    public boolean isAllowDuplicates()
    {
        return allowDuplicates;
    }

    public boolean isAllowInterpolation()
    {
        return allowInterpolation;
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

    /**
     * Parse INI text
     *
     * @param reader
     *            the {@link BufferedReader} to read the INI text from
     * @return this Ini
     * @throws IOException
     *             When errors are encountered while reading from reader
     * @throws IniParserException
     *             When the INI text read is invalid in some way.
     */
    public Ini read(BufferedReader reader) throws IOException, IniParserException
    {
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

            if (value.isEmpty())
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
                    // Empty line marks the end of a value
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
                                currSectionName = null;
                                currSection = null;
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
                                lineNumberMap.put(currSectionName + ":" + currOptionName, lineNo);
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

                if (unjoinedOptionValue.size() > 0)
                {
                    // Remove trailing whitespace lines
                    ListIterator<String> iter = unjoinedOptionValue.listIterator(unjoinedOptionValue.size());
                    while (iter.hasPrevious())
                        if (StringUtil.strip(iter.previous()).isEmpty())
                            iter.remove();
                        else
                            break;

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

        if (allowInterpolation)
        {
            interpolate ();
            if (parsingErrors.size() > 0)
                throw new IniParserException(parsingErrors);
        }

        return this;
    }

    /**
     * Parse an INI file with the default {@link Charset}
     *
     * @param iniPath
     *            The {@link Path} pointing the the INI file to read
     * @return this Ini
     * @throws IOException
     *             When errors are encountered while reading from reader
     * @throws IniParserException
     *             When the INI text read is invalid in some way.
     * @see StandardCharsets#UTF_8
     */
    public Ini read(Path iniPath) throws IOException, IniParserException
    {
        read(iniPath, StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Parse an INI file with a specified {@link Charset}
     *
     * @param iniPath
     *            The {@link Path} pointing the the INI file to read
     * @param charset
     *            The {@link Charset} to use when reading the file
     * @return this Ini
     * @throws IOException
     *             When errors are encountered while reading from reader
     * @throws IniParserException
     *             When the INI text read is invalid in some way.
     * @see StandardCharsets
     */
    public Ini read(Path iniPath, Charset charset) throws IOException, IniParserException
    {
        try (BufferedReader reader = Files.newBufferedReader(iniPath, charset))
        {
            read(reader);
        }
        return this;
    }

    /**
     * Set if duplicate sections and options will be accepted, or throw a {@link IniParserException} at
     * {@link #read(BufferedReader)} time.
     *
     * @param allowDuplicates
     *            duplicates are accepted iff true
     * @return this Ini
     */
    public Ini setAllowDuplicates(boolean allowDuplicates)
    {
        this.allowDuplicates = allowDuplicates;
        return this;
    }

    /**
     * Set if interpolation of values should be performed. Values containing the string "${section:option}"
     * will be substituted for the value of the given option in the given section. Errors encountered
     * parsing interpolations will result in a {@link IniParserException} being thrown at
     * {@link #read(BufferedReader)} time.
     *
     * @param allowInterpolation
     *            values will be interpolated iff true
     * @return this Ini
     */
    public Ini setAllowInterpolation(boolean allowInterpolation)
    {
        this.allowInterpolation = allowInterpolation;
        return this;
    }

    /**
     * Set if option keys with no values will be accepted, or throw a {@link IniParserException} at
     * {@link #read(BufferedReader)} time.
     *
     * @param allowNoValue
     *            no value options are accepted iff true
     * @return this Ini
     */
    public Ini setAllowNoValue(boolean allowNoValue)
    {
        this.allowNoValue = allowNoValue;

        compileOptionPattern();
        return this;
    }

    /**
     * Set which {@link String}s should start full comment lines.
     *
     * @param commentPrefixes
     *            the full line comment prefixes
     * @return this Ini
     */
    public Ini setCommentPrefixes(List<String> commentPrefixes)
    {
        this.commentPrefixes = commentPrefixes;
        return this;
    }

    /**
     * Set which {@link String}s should divide option keys from values
     *
     * @param delimiters
     *            the key/value delimiters
     * @return this Ini
     */
    public Ini setDelimiters(List<String> delimiters)
    {
        this.delimiters = delimiters;

        compileOptionPattern();
        return this;
    }

    /**
     * Set if empty lines should be considered to be a part of the latest option's value or not. Can cause
     * {@link IniParserException} to be thrown in some cases when false
     *
     * @param emptyLinesInValues
     *            empty lines are added to option values iff true
     * @return this Ini
     */
    public Ini setEmptyLinesInValues(boolean emptyLinesInValues)
    {
        this.emptyLinesInValues = emptyLinesInValues;
        return this;
    }

    /**
     * Set which {@link String}s should divide data from comments on non-blank lines
     *
     * @param inlineCommentPrefixes
     *            the partial line comment prefixes
     * @return this Ini
     */
    public Ini setInlineCommentPrefixes(List<String> inlineCommentPrefixes)
    {
        this.inlineCommentPrefixes = inlineCommentPrefixes;
        return this;
    }

    /**
     * Set if spaces should be placed around option key/value delimiters when writing
     *
     * @param spaceAroundDelimiters
     *            place spaces around key/value delimiters iff true
     * @return this Ini
     */
    public Ini setSpaceAroundDelimiters(boolean spaceAroundDelimiters)
    {
        this.spaceAroundDelimiters = spaceAroundDelimiters;
        return this;
    }

    /**
     * Write INI formatted text
     *
     * @param writer
     *            the {@link BufferedWriter} to write the INI text to
     * @return this Ini
     * @throws IOException
     *             When errors are encountered while writing to the writer
     */
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

                // If interpolation is enabled, find the original value instead of
                // the interpolated one.
                if (allowInterpolation)
                {
                    String rawKey = sectionName + ":" + option.toLowerCase();
                    if (rawValues.containsKey (rawKey))
                    {
                        value = rawValues.get (rawKey);
                    }
                }

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

    /**
     * Write an INI file with the default {@link Charset}
     *
     * @param iniPath
     *            The {@link Path} pointing the the INI file to write
     * @return this Ini
     * @throws IOException
     *             When errors are encountered while writing to the writer
     * @see StandardCharsets#UTF_8
     */
    public Ini write(Path iniPath) throws IOException
    {
        write(iniPath, StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Write an INI file with a specified {@link Charset}
     *
     * @param iniPath
     *            The {@link Path} pointing the the INI file to write
     * @param charset
     *            The {@link Charset} to use when writing the file
     * @return this Ini
     * @throws IOException
     *             When errors are encountered while writing to the writer
     * @see StandardCharsets
     */
    public Ini write(Path iniPath, Charset charset) throws IOException
    {
        try (BufferedWriter writer = Files.newBufferedWriter(iniPath, charset))
        {
            write(writer);
        }
        return this;
    }
}
