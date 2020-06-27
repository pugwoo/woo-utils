package com.pugwoo.wooutils.string;

import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

public class TestRegexUtils {

    @Test
    public void testMatch() {
        assert RegexUtils.isMatch("hello", "h");
        assert !RegexUtils.isMatch("hello", "a");

        assert RegexUtils.isMatch("application-prod.yaml", ".*prod.*\\.(yaml|properties)$");
        assert RegexUtils.isMatch("application-prod.properties", ".*prod.*\\.(yaml|properties)$");

        // 测试大小写
        assert !RegexUtils.isMatch("Hello", "h");
        assert RegexUtils.isMatch("Hello", "(?i)h");

        Pattern pattern = Pattern.compile("h");
        assert RegexUtils.isMatch("hello", pattern);
        assert !RegexUtils.isMatch("aaa", pattern);
    }

    @Test
    public void testMatchGroup() {
        assert RegexUtils.getFirstMatchStr("hello", "h").equals("h");
        assert RegexUtils.getFirstMatchStr("hello", "(h)e").equals("h");
        assert RegexUtils.getFirstMatchStr("hello", "(h)e.*(o)").equals("ho");


        List<String> list = RegexUtils.getAllMatchStr("hello", "l");
        assert list.size() == 2 && list.get(0).equals("l") && list.get(1).equals("l");
    }
    
    @Test
    public void testReplaceAllGroup() {
        assert "__====__====__".equals(RegexUtils.replaceAllGroup("__aaaa__aaaa__", "_aaaa_", "_====_"));
        assert "__a==a__a==a__".equals(RegexUtils.replaceAllGroup("__aaaa__aaaa__", "_a(aa)a_", "=="));
        assert "__a= a__a= a__".equals(RegexUtils.replaceAllGroup("__aaaa__aaaa__", "_a(a)(a)a_", "=", " "));
        
        assert "__{aaaa}__{aaaa}__".equals(RegexUtils.replaceAllGroup("__aaaa__aaaa__", "aaaa", group -> "{" + group + "}"));
        assert "__a{aa}a__a{aa}a__".equals(RegexUtils.replaceAllGroup("__aaaa__aaaa__", "_a(aa)a_", group -> "{" + group + "}"));
        assert "__a{a}{1}a__a{a}{1}a__".equals(RegexUtils.replaceAllGroup("__aaaa__aaaa__", "_a(a)(a)a_", group -> "{" + group + "}", group -> "{1}"));
        
        assert "__aaaaa__a{b}a__".equals(RegexUtils.replaceAllGroup("__aaaaa__azbaa__", "_a(\\S)([bc])(\\S)a_", "{", null, "}"));
    }
    
    @Test
    public void testReplaceFirstGroup() {
        assert "__====__aaaa__".equals(RegexUtils.replaceFirstGroup("__aaaa__aaaa__", "_aaaa_", "_====_"));
        assert "__a==a__aaaa__".equals(RegexUtils.replaceFirstGroup("__aaaa__aaaa__", "_a(aa)a_", "=="));
        assert "__a= a__aaaa__".equals(RegexUtils.replaceFirstGroup("__aaaa__aaaa__", "_a(a)(a)a_", "=", " "));
        
        assert "__{aaaa}__aaaa__".equals(RegexUtils.replaceFirstGroup("__aaaa__aaaa__", "aaaa", group -> "{" + group + "}"));
        assert "__a{aa}a__aaaa__".equals(RegexUtils.replaceFirstGroup("__aaaa__aaaa__", "_a(aa)a_", group -> "{" + group + "}"));
        assert "__a{a}{1}a__aaaa__".equals(RegexUtils.replaceFirstGroup("__aaaa__aaaa__", "_a(a)(a)a_", group -> "{" + group + "}", group -> "{1}"));
    }
}
