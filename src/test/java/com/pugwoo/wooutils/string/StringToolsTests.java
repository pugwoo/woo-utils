package com.pugwoo.wooutils.string;

import org.junit.Test;

public class StringToolsTests {


    @Test
    public void testBlank() {
        assert StringTools.isBlank(" ");
        assert StringTools.isBlank("");
        assert StringTools.isBlank(" \t ");
        assert StringTools.isBlank(" \r ");
        assert StringTools.isBlank(" \n ");
        assert StringTools.isBlank(" \r\n\t ");
        assert !StringTools.isBlank(" a ");
    }

    @Test
    public void testDigit() {
        assert StringTools.isDigit("0245215");
        assert !StringTools.isDigit("024.5215");
        assert !StringTools.isDigit("0245a215");
        assert !StringTools.isDigit("02我45215");
        assert !StringTools.isDigit("");
        assert !StringTools.isDigit(null);
        assert !StringTools.isDigit(" ");
    }

    @Test
    public void testAlphabeticOrDigit() {
        assert StringTools.isAlphabeticOrDigit("abc");
        assert StringTools.isAlphabeticOrDigit("111");
        assert StringTools.isAlphabeticOrDigit("ab42c54");
        assert !StringTools.isAlphabeticOrDigit("ab$c");
        assert !StringTools.isAlphabeticOrDigit("a5.4bc");
        assert !StringTools.isAlphabeticOrDigit("");
        assert !StringTools.isAlphabeticOrDigit(null);
    }

    @Test
    public void testReplaceAllGroup() {
        assert "__====__====__".equals(StringTools.replaceAllGroup("__aaaa__aaaa__", "_aaaa_", "_====_"));
        assert "__a==a__a==a__".equals(StringTools.replaceAllGroup("__aaaa__aaaa__", "_a(aa)a_", "=="));
        assert "__a= a__a= a__".equals(StringTools.replaceAllGroup("__aaaa__aaaa__", "_a(a)(a)a_", "=", " "));
        
        assert "__{aaaa}__{aaaa}__".equals(StringTools.replaceAllGroup("__aaaa__aaaa__", "aaaa", group -> "{" + group + "}"));
        assert "__a{aa}a__a{aa}a__".equals(StringTools.replaceAllGroup("__aaaa__aaaa__", "_a(aa)a_", group -> "{" + group + "}"));
        assert "__a{a}{1}a__a{a}{1}a__".equals(StringTools.replaceAllGroup("__aaaa__aaaa__", "_a(a)(a)a_", group -> "{" + group + "}", group -> "{1}"));

        assert "__aaaaa__a{b}a__".equals(StringTools.replaceAllGroup("__aaaaa__azbaa__", "_a(\\S)([bc])(\\S)a_", "{", null, "}"));
    }
    
    @Test
    public void testReplaceFirstGroup() {
        assert "__====__aaaa__".equals(StringTools.replaceFirstGroup("__aaaa__aaaa__", "_aaaa_", "_====_"));
        assert "__a==a__aaaa__".equals(StringTools.replaceFirstGroup("__aaaa__aaaa__", "_a(aa)a_", "=="));
        assert "__a= a__aaaa__".equals(StringTools.replaceFirstGroup("__aaaa__aaaa__", "_a(a)(a)a_", "=", " "));
    
        assert "__{aaaa}__aaaa__".equals(StringTools.replaceFirstGroup("__aaaa__aaaa__", "aaaa", group -> "{" + group + "}"));
        assert "__a{aa}a__aaaa__".equals(StringTools.replaceFirstGroup("__aaaa__aaaa__", "_a(aa)a_", group -> "{" + group + "}"));
        assert "__a{a}{1}a__aaaa__".equals(StringTools.replaceFirstGroup("__aaaa__aaaa__", "_a(a)(a)a_", group -> "{" + group + "}", group -> "{1}"));
    }
}
