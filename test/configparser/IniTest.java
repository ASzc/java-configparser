package configparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import configparser.exceptions.DuplicateOptionError;
import configparser.exceptions.DuplicateSectionError;
import configparser.exceptions.IniParserException;
import configparser.exceptions.InvalidLine;
import configparser.exceptions.MissingSectionHeaderError;
import configparser.exceptions.ParsingError;

public class IniTest
{
    public static final Path outputRoot = Paths.get("target", "test-classes");

    private static final Path resourcesRoot = Paths.get("test", "resources");

    private static final Path resPythonImplScript = resourcesRoot.resolve("configparser-rw.py");

    private static boolean compareOutputs(Path iniInput) throws IOException
    {
        Path pythonOutput = writePython(iniInput);
        Path javaOutput = writeJava(iniInput);

        return diff(pythonOutput, javaOutput);
    }

    private static boolean diff(Path former, Path latter) throws IOException
    {
        ProcessBuilder pb = new ProcessBuilder("diff", "-u", former.toString(), latter.toString());
        Process process = pb.start();
        int exitCode = -1;
        try
        {
            exitCode = process.waitFor();
        } catch (InterruptedException e)
        {
            process.destroy();
            throw new IOException(e);
        }

        if (exitCode > 0)
        {
            try (BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                String line;
                while ((line = stdout.readLine()) != null)
                    System.out.println(line);
            }

            return false;
        }

        return true;
    }

    private static void readWithExpectedErrors(Ini ini, Path iniInput, List<ParsingError> expectedErrors)
            throws IOException
    {
        try
        {
            ini.read(iniInput);
            Assert.fail("Did not throw IniParserException");
        } catch (IniParserException e)
        {
            List<ParsingError> parsingErrors = e.getParsingErrors();
            // Order matters
            if (!parsingErrors.equals(expectedErrors))
            {
                Assert.fail("Non-expected ParsingErrors were generated: " + e);
            }
        }
    }

    private static void readWithExpectedErrors(Path iniInput, List<ParsingError> expectedErrors) throws IOException
    {
        readWithExpectedErrors(new Ini(), iniInput, expectedErrors);
    }

    private static Path writeJava(Path iniInput) throws IOException
    {
        Path javaOutput = outputRoot.resolve(iniInput.getFileName() + "-java");

        new Ini().read(iniInput).write(javaOutput);

        return javaOutput;
    }

    private static Path writePython(Path iniInput) throws IOException
    {
        Path pythonOutput = outputRoot.resolve(iniInput.getFileName() + "-python");

        ProcessBuilder pb = new ProcessBuilder(resPythonImplScript.toString(), iniInput.toString(),
                pythonOutput.toString());
        Process process = pb.start();
        try
        {
            process.waitFor();
        } catch (InterruptedException e)
        {
            process.destroy();
            throw new IOException(e);
        }

        return pythonOutput;
    }

    @Test
    public void allowDuplicates() throws IOException
    {
        Path cfg = resourcesRoot.resolve("docs-example-duplicates.cfg");

        new Ini().setAllowDuplicates(true).read(cfg);
    }

    @Test
    public void allowNoValue() throws IOException
    {
        Path cfg = resourcesRoot.resolve("docs-example.cfg");

        new Ini().setAllowNoValue(true).read(cfg);
    }

    @Test
    public void checkAgainstReferenceImpl() throws IOException
    {
        Path cfg = resourcesRoot.resolve("docs-example-default.cfg");

        Assert.assertTrue("The outputs of python and java differ", compareOutputs(cfg));
    }

    @Test
    public void disallowDuplicates() throws IOException
    {
        Path cfg = resourcesRoot.resolve("docs-example-duplicates.cfg");
        List<ParsingError> expectedErrors = new LinkedList<>();

        expectedErrors.add(new DuplicateOptionError(12, "All Values Are Strings", "are they treated as numbers?"));
        expectedErrors.add(new DuplicateSectionError(23, "No Values"));
        expectedErrors.add(new MissingSectionHeaderError(24, "empty string value here ="));

        readWithExpectedErrors(cfg, expectedErrors);
    }

    @Test
    public void disallowNoValue() throws IOException
    {
        Path cfg = resourcesRoot.resolve("docs-example.cfg");
        List<ParsingError> expectedErrors = new LinkedList<>();

        expectedErrors.add(new InvalidLine(20, "key_without_value"));

        readWithExpectedErrors(cfg, expectedErrors);
    }

    @Test
    public void emptyLinesInValues() throws IOException
    {
        Path cfg = resourcesRoot.resolve("docs-example-emptylines.cfg");

        new Ini().read(cfg);
    }

    @Test
    public void missingSectionHeader() throws IOException
    {
        Path cfg = resourcesRoot.resolve("docs-example-missingsection.cfg");
        List<ParsingError> expectedErrors = new LinkedList<>();

        expectedErrors.add(new MissingSectionHeaderError(2, "option = value"));

        readWithExpectedErrors(cfg, expectedErrors);
    }

    @Test
    public void noEmptyLinesInValues() throws IOException
    {
        Path cfg = resourcesRoot.resolve("docs-example-emptylines.cfg");
        List<ParsingError> expectedErrors = new LinkedList<>();

        expectedErrors.add(new InvalidLine(14, "    multiline1"));
        expectedErrors.add(new InvalidLine(17, "    multiline2"));

        readWithExpectedErrors(new Ini().setEmptyLinesInValues(false), cfg, expectedErrors);
    }
}
