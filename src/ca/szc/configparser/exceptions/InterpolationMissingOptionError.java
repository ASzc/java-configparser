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
 * This exception is thrown if the interpolation references a non-existent option.
 */
public class InterpolationMissingOptionError extends ParsingError
{
    private String option;
    private String section;
    private String value;
    private String missingOption;

    public InterpolationMissingOptionError (int lineNo, String option, String section, String value, String missingOption)
    {
        super(lineNo);
        this.option = option;
        this.section = section;
        this.value = value;
        this.missingOption = missingOption;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        InterpolationMissingOptionError other = (InterpolationMissingOptionError) obj;

        return (section == null ? other.section == null : section.equals(other.section)) &&
               (option == null ? other.option == null : option.equals(other.option)) &&
               (value == null ? other.value == null : value.equals(other.value)) &&
               (missingOption == null ? other.missingOption == null : missingOption.equals (other.missingOption));
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Interpolation error in section '");
        sb.append(getSectionName ());
        sb.append("', option '");
        sb.append(getOptionName ());
        sb.append("': '");
        sb.append(getValue ());
        sb.append("' is missing option '");
        sb.append(getMissingOption ());
        sb.append("'");

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

    public String getValue ()
    {
        return value;
    }

    public String getMissingOption ()
    {
        return missingOption;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((option == null) ? 0 : option.hashCode());
        result = prime * result + ((section == null) ? 0 : section.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((missingOption == null) ? 0 : missingOption.hashCode());

        return result;
    }
}
