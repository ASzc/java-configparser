# java-configparser: a Python-compatible Java INI parser

## Overview

java-configparser is an Apache 2.0 licensed INI file parser for Java. It is intended to be compatible with Python's [configparser package](https://docs.python.org/3/library/configparser.html#supported-ini-file-structure).

Java 7 or higher is required.

## Usage

Here's an example file that uses java-configparser to read an INI file, make some changes and write it out elsewhere:

    TODO

### Maven POM

For maven, add an entry in your pom.xml file:

    <dependencies>
      <dependency>
        <groupId>ca.szc.configparser</groupId>
        <artifactId>java-configparser</artifactId>
        <version>0.1</version>
      </dependency>
    </dependencies>

## Building

    git clone git://github.com/ASzc/java-configparser.git
    cd java-configparser
    mvn install

### Tests

Some tests require the `python3` and `diff` command line tools, and therefore may not run correctly on all operating systems. On Linux, ensure your distro's equivalent of Fedora's `python3` and `diffutils` packages are installed.
