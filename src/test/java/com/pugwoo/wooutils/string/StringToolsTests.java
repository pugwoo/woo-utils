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

}
