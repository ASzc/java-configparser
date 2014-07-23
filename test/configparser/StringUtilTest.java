package configparser;

import org.junit.Assert;
import org.junit.Test;

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
