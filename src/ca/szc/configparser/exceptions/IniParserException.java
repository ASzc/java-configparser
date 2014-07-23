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

import java.io.IOException;
import java.util.List;

public class IniParserException extends IOException
{
    private static final long serialVersionUID = -1241708876764785452L;

    private static String createMessage(List<ParsingError> parsingErrors)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Source contains parsing errors:");
        for (ParsingError error : parsingErrors)
        {
            sb.append("\n\t");
            sb.append("[line ");
            sb.append(error.getLineNo());
            sb.append("]: ");
            sb.append(error.getMessage());
        }

        return sb.toString();
    }

    private final List<ParsingError> parsingErrors;

    public IniParserException(List<ParsingError> parsingErrors)
    {
        super(createMessage(parsingErrors));
        this.parsingErrors = parsingErrors;
    }

    public List<ParsingError> getParsingErrors()
    {
        return parsingErrors;
    }
}
