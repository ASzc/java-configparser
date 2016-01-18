/**
 * Copyright 2016 Red Hat Inc.
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
 * This exception is thrown if there is a syntax error in the value interpolation.
 */
public class InterpolationSyntaxError extends ParsingError
{
    private String option;
    private String section;
    private String message;

    public InterpolationSyntaxError (int lineNo, String option, String section, String message)
    {
        super(lineNo);
        this.option = option;
        this.section = section;
        this.message = message;
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
        InterpolationSyntaxError other = (InterpolationSyntaxError) obj;

        return (section == null ? other.section == null : section.equals(other.section)) &&
               (option == null ? other.option == null : option.equals(other.option)) &&
               (message == null ? other.message == null : message.equals(other.message));
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Interpolation syntax error in section '");
        sb.append(getSectionName ());
        sb.append("', option '");
        sb.append(getOptionName ());
        sb.append("': ");
        sb.append(message);

        return sb.toString();
    }

    public String getOptionName()
    {
        return option;
    }

    public String getSectionName()
    {
        return section;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((option == null) ? 0 : option.hashCode());
        result = prime * result + ((section == null) ? 0 : section.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());

        return result;
    }
}
