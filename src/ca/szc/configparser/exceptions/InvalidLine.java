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
package ca.szc.configparser.exceptions;

/**
 * Defines the location and information about an invalid line
 */
public class InvalidLine extends ParsingError
{
    private final String line;

    public InvalidLine(int lineNo, String line)
    {
        super(lineNo);
        this.line = line;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        InvalidLine other = (InvalidLine) obj;
        if (line == null)
        {
            if (other.line != null)
                return false;
        }
        else if (!line.equals(other.line))
            return false;
        return true;
    }

    public String getLine()
    {
        return line;
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Invalid: ");
        sb.append(getLine());

        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((line == null) ? 0 : line.hashCode());
        return result;
    }
}
