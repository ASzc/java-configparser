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
 * Defines the location and information about a missing section header
 */
public class MissingSectionHeaderError extends InvalidLine
{
    public MissingSectionHeaderError(int lineNo, String line)
    {
        super(lineNo, line);
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("option ");
        sb.append(getLine());
        sb.append(" is declared outside of a section header");

        return sb.toString();
    }
}
