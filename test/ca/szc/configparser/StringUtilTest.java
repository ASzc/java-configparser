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
package ca.szc.configparser;

import org.junit.Assert;
import org.junit.Test;

import ca.szc.configparser.StringUtil;

public class StringUtilTest
{
    @Test
    public void testRstrip()
    {
        Assert.assertEquals("Did not remove whitespace on right", "asd", StringUtil.rstrip("asd  \n \f  \t\n "));
        Assert.assertEquals("Did not preserve whitespace on left", "   asd", StringUtil.rstrip("   asd"));
        Assert.assertEquals("Did not preserve whitespace on left with whitespace present on right", " \t  asd",
                StringUtil.rstrip(" \t  asd \n \f  \t\n "));
        Assert.assertEquals("Did not remove all from 4 whitespace string", "", StringUtil.rstrip("    "));
        Assert.assertEquals("Did not remove all from 3 whitespace string", "", StringUtil.rstrip("   "));
        Assert.assertEquals("Did not remove all from 2 whitespace string", "", StringUtil.rstrip("  "));
        Assert.assertEquals("Did not remove all from 1 whitespace string", "", StringUtil.rstrip(" "));
    }

    @Test
    public void testStrip()
    {
        Assert.assertEquals("Did not remove whitespace on right", "asd", StringUtil.strip("asd  \n \f  \t\n "));
        Assert.assertEquals("Did not remove whitespace on left", "asd", StringUtil.strip("   asd"));
        Assert.assertEquals("Did not remove whitespace on both sides with whitespace present on both sides", "asd",
                StringUtil.strip("  \f \t asd \n \f  \t\n "));
        Assert.assertEquals("Did not keep whitespace in middle", "asd asd \f asd",
                StringUtil.strip("   asd asd \f asd \t"));
        Assert.assertEquals("Did not remove all from 4 whitespace string", "", StringUtil.strip("    "));
        Assert.assertEquals("Did not remove all from 3 whitespace string", "", StringUtil.strip("   "));
        Assert.assertEquals("Did not remove all from 2 whitespace string", "", StringUtil.strip("  "));
        Assert.assertEquals("Did not remove all from 1 whitespace string", "", StringUtil.strip(" "));
    }
}
