/**
 * Copyright 2014 Red Hat Inc.
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ca.szc.configparser.Ini;
import ca.szc.configparser.exceptions.DuplicateOptionError;
import ca.szc.configparser.exceptions.DuplicateSectionError;
import ca.szc.configparser.exceptions.IniParserException;
import ca.szc.configparser.exceptions.InterpolationMissingOptionError;
import ca.szc.configparser.exceptions.InterpolationSyntaxError;
import ca.szc.configparser.exceptions.InvalidLine;
import ca.szc.configparser.exceptions.MissingSectionHeaderError;
import ca.szc.configparser.exceptions.ParsingError;

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
    public void allowInterpolation () throws IOException
    {
        Path cfg = resourcesRoot.resolve ("interpolation.cfg");

        Ini ini = new Ini().setAllowInterpolation(true).read(cfg);

        try
        {
            Assert.assertEquals("Paul", ini.getValue("common", "favourite Beatle"));
            Assert.assertEquals("green", ini.getValue("common", "favourite color"));
            Assert.assertEquals("green day", ini.getValue("tom", "favourite band"));
            Assert.assertEquals("John Paul II", ini.getValue("tom", "favourite pope"));
            Assert.assertEquals("John Paul III", ini.getValue("tom", "sequel"));
            Assert.assertEquals("George", ini.getValue("ambv", "favourite Beatle"));
            Assert.assertEquals("George V", ini.getValue("ambv", "son of Edward VII"));
            Assert.assertEquals("George VI", ini.getValue("ambv", "son of George V"));
            Assert.assertEquals("George", ini.getValue("stanley","favourite Beatle" ));
            Assert.assertEquals("black", ini.getValue("stanley","favourite color" ));
            Assert.assertEquals("paranoid", ini.getValue("stanley","favourite state of mind" ));
            Assert.assertEquals("soylent green", ini.getValue("stanley","favourite movie" ));
            Assert.assertEquals("John Paul II", ini.getValue("stanley","favourite pope" ));
            Assert.assertEquals("black sabbath - paranoid", ini.getValue("stanley","favourite song" ));
        }
        catch (Exception ex)
        {
            Assert.fail(ex.getMessage());
        }
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
    public void interpolationErrors() throws IOException
    {
        Path cfg = resourcesRoot.resolve ("interpolation-errors.cfg");

        try
        {
            Ini ini = new Ini().setAllowInterpolation(true).read(cfg);
            Assert.fail();
        }
        catch (IniParserException ex)
        {
            List<ParsingError> errors = ex.getParsingErrors();
            Assert.assertEquals(5, errors.size());

            ParsingError error = errors.get(0);
            Assert.assertEquals(true, error instanceof InterpolationSyntaxError);
            Assert.assertEquals("case1", ((InterpolationSyntaxError) error).getOptionName());

            error = errors.get(1);
            Assert.assertEquals(true, error instanceof InterpolationMissingOptionError);
            Assert.assertEquals("case2", ((InterpolationMissingOptionError) error).getOptionName());

            error = errors.get(2);
            Assert.assertEquals(true, error instanceof InterpolationMissingOptionError);
            Assert.assertEquals("case3", ((InterpolationMissingOptionError) error).getOptionName());

            error = errors.get(3);
            Assert.assertEquals(true, error instanceof InterpolationSyntaxError);
            Assert.assertEquals("case4", ((InterpolationSyntaxError) error).getOptionName());

            error = errors.get(4);
            Assert.assertEquals(true, error instanceof InterpolationSyntaxError);
            Assert.assertEquals("case5", ((InterpolationSyntaxError) error).getOptionName());
        }
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

    @Test
    public void writeInterpolation() throws IOException
    {
        Path cfg = resourcesRoot.resolve("interpolation.cfg");
        Path outputCfg = outputRoot.resolve ("interpolation-2.cfg");

        Ini ini = new Ini().setAllowInterpolation(true).read(cfg).write(outputCfg);

        ini = new Ini().setAllowInterpolation(false).read(outputCfg);

        try
        {
            Assert.assertEquals("Paul", ini.getValue("common", "favourite Beatle"));
            Assert.assertEquals("green", ini.getValue("common", "favourite color"));
            Assert.assertEquals("${common:favourite color} day", ini.getValue("tom", "favourite band"));
            Assert.assertEquals("John ${common:favourite Beatle} II", ini.getValue("tom", "favourite pope"));
            Assert.assertEquals("${favourite pope}I", ini.getValue("tom", "sequel"));
            Assert.assertEquals("George", ini.getValue("ambv", "favourite Beatle"));
            Assert.assertEquals("${favourite Beatle} V", ini.getValue("ambv", "son of Edward VII"));
            Assert.assertEquals("${son of Edward VII}I", ini.getValue("ambv", "son of George V"));
            Assert.assertEquals("${ambv:favourite Beatle}", ini.getValue("stanley","favourite Beatle" ));
            Assert.assertEquals("black", ini.getValue("stanley","favourite color" ));
            Assert.assertEquals("paranoid", ini.getValue("stanley","favourite state of mind" ));
            Assert.assertEquals("soylent ${common:favourite color}", ini.getValue("stanley","favourite movie" ));
            Assert.assertEquals("${tom:favourite pope}", ini.getValue("stanley","favourite pope" ));
            Assert.assertEquals("${favourite color} sabbath - ${favourite state of mind}", ini.getValue("stanley","favourite song" ));
        }
        catch (Exception ex)
        {
            Assert.fail(ex.getMessage());
        }

        outputCfg.toFile().delete();
    }
}
