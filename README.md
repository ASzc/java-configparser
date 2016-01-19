# java-configparser: a Python-compatible Java INI parser

## Overview

java-configparser is an Apache 2.0 licensed INI file parser for Java. It is intended to be compatible with Python's [configparser package](https://docs.python.org/3/library/configparser.html#supported-ini-file-structure).

Java 7 or higher is required.

## Usage

Here's an example file that uses java-configparser to read an INI file, make some changes and write it out elsewhere:

    import java.io.IOException;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.util.Map;

    import ca.szc.configparser.Ini;
    import ca.szc.configparser.exceptions.IniParserException;

    public class Demo
    {
        public Demo() throws IniParserException, IOException
        {
            // Read from a file
            Path input = Paths.get("demo.cfg");
            Ini ini = new Ini().read(input);
            Map<String, Map<String, String>> sections = ini.getSections();

            // Change option secret in section Secrets to xyzzy
            sections.get("Secrets").put("secret", "xyzzy");

            // Change all options called foo to bar
            for (Map<String, String> section : sections.values())
            {
                if (section.containsKey("foo"))
                    section.put("foo", "bar");
            }

            // Write to a new file
            // Note: the exact formatting of the original file is not preserved when writing
            Path output = Paths.get("demo-modified.cfg");
            ini.write(output);
        }
    }

### Maven POM

For maven, add an entry in your pom.xml file:

    <dependencies>
      <dependency>
        <groupId>ca.szc.configparser</groupId>
        <artifactId>java-configparser</artifactId>
        <version>0.2</version>
      </dependency>
    </dependencies>

## Changelog

### 0.2

- Implemented interpolation (Darren Coleman)

### 0.1

- Initial release

## Building

    git clone git://github.com/ASzc/java-configparser.git
    cd java-configparser
    mvn install

### Tests

Some tests require the `python3` and `diff` command line tools, and therefore may not run correctly on all operating systems. On Linux, ensure your distro's equivalent of Fedora's `python3` and `diffutils` packages are installed.
