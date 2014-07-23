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

import java.io.IOException;
import java.nio.file.Paths;

import ca.szc.configparser.exceptions.IniParserException;

public class Main
{
    public static void main(String[] args) throws IniParserException, IOException
    {
        new Ini().read(Paths.get(args[0])).write(Paths.get(args[1]));
    }
}
