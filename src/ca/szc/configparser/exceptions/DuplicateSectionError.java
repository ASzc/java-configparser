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

public class DuplicateSectionError extends ParsingError
{
    private final String sectionName;

    public DuplicateSectionError(int lineNo, String sectionName)
    {
        super(lineNo);
        this.sectionName = sectionName;
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
        DuplicateSectionError other = (DuplicateSectionError) obj;
        if (sectionName == null)
        {
            if (other.sectionName != null)
                return false;
        }
        else if (!sectionName.equals(other.sectionName))
            return false;
        return true;
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("section ");
        sb.append(getSectionName());
        sb.append(" already exists");

        return sb.toString();
    }

    public String getSectionName()
    {
        return sectionName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sectionName == null) ? 0 : sectionName.hashCode());
        return result;
    }
}
