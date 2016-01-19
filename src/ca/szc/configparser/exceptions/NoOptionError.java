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
package ca.szc.configparser.exceptions;

/**
 * This exception is thrown when looking up a non-existant option from an interpolation.
 */
public class NoOptionError extends Exception
{
    private String section;
    private String option;

    public NoOptionError (String section, String option)
    {
        super("No option exists with name '" + option + "' in section '" + section + "'");
        this.section = section;
        this.option = option;
    }

    public String getSectionName()
    {
        return section;
    }

    public String getOption()
    {
        return option;
    }
}
