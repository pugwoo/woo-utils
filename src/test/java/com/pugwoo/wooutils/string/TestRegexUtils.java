package com.pugwoo.wooutils.string;

import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

public class TestRegexUtils {

    @Test
    public void testMatch() {
        assert RegexUtils.isMatch("hello", "h");
        assert !RegexUtils.isMatch("hello", "a");

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


}
