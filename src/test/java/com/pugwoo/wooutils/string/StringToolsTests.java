package com.pugwoo.wooutils.string;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.lang.EqualUtils;
import org.junit.Test;

import java.util.List;

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

        assert StringTools.isAnyEmpty();
        assert StringTools.isAnyEmpty("a", null);
        assert StringTools.isAnyEmpty("");
        assert StringTools.isAnyEmpty("a", "", "b");
        assert !StringTools.isAnyEmpty("a");
        assert !StringTools.isAnyEmpty("a", "b");

        assert StringTools.isAnyEmpty(ListUtils.newArrayList());
        assert StringTools.isAnyEmpty(ListUtils.newArrayList(null));
        assert StringTools.isAnyEmpty(ListUtils.newArrayList("a", "", "b"));
        assert StringTools.isAnyEmpty(ListUtils.newArrayList(""));
        assert !StringTools.isAnyEmpty(ListUtils.newArrayList("a"));
        assert !StringTools.isAnyEmpty(ListUtils.newArrayList("a", "b"));

        assert StringTools.isAnyBlank();
        assert StringTools.isAnyBlank("a", null);
        assert StringTools.isAnyBlank("  ");
        assert StringTools.isAnyBlank("a", "  ", "b");
        assert !StringTools.isAnyBlank("a");
        assert !StringTools.isAnyBlank("a", "b");

        assert StringTools.isAnyBlank(ListUtils.newArrayList());
        assert StringTools.isAnyBlank(ListUtils.newArrayList(null));
        assert StringTools.isAnyBlank(ListUtils.newArrayList("a", "  ", "b"));
        assert StringTools.isAnyBlank(ListUtils.newArrayList("  "));
        assert !StringTools.isAnyBlank(ListUtils.newArrayList("a"));
        assert !StringTools.isAnyBlank(ListUtils.newArrayList("a", "b"));
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
    public void testSplit() {
        List<String> strings = StringTools.splitAndFilter("a;b;c;;;d",
                ";", o -> StringTools.isNotBlank(o));
        EqualUtils equalUtils = new EqualUtils();
        equalUtils.ignoreListOrder(true);
        assert equalUtils.isEqual(strings, ListUtils.newArrayList("a","b","c","d"));
    }

    @Test
    public void testJoin() {
        assert "a,b,c".equals(StringTools.join(",", "a", "b", "c"));
        assert "a,b,c".equals(StringTools.join(",", ListUtils.newList("a", "b", "c")));
        assert "a,b,c".equals(StringTools.join(new String[]{"a", "b", "c"}, ","));
        assert "a,b,c".equals(StringTools.join( ListUtils.newList("a", "b", "c"), ","));
    }
}
