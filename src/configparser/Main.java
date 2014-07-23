package configparser;

import java.io.IOException;
import java.nio.file.Paths;

import configparser.exceptions.IniParserException;

public class Main
{
    public static void main(String[] args) throws IniParserException, IOException
    {
        new Ini().read(Paths.get(args[0])).write(Paths.get(args[1]));
    }
}
