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

/**
 * String methods that are more compatible with Python's
 */
public class StringUtil
{
    /**
     * Return a copy of a String with all whitespace characters removed from the end. Whitespace as defined by
     * {@link Character#isWhitespace(char)}
     */
    public static String rstrip(String s)
    {
        char[] val = s.toCharArray();

        int start = 0;
        int length = val.length;

        while ((start < length) && (Character.isWhitespace(val[length - 1])))
            length--;

        return s.substring(start, length);
    }

    /**
     * Return a copy of a String with all whitespace characters removed from the beginning and end. Whitespace as
     * defined by {@link Character#isWhitespace(char)}
     */
    public static String strip(String s)
    {
        char[] val = s.toCharArray();

        int start = 0;
        int length = val.length;

        while ((start < length) && (Character.isWhitespace(val[start])))
            start++;
        while ((start < length) && (Character.isWhitespace(val[length - 1])))
            length--;

        return s.substring(start, length);
    }
}
