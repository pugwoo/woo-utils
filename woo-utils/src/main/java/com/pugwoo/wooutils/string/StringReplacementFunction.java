package com.pugwoo.wooutils.string;

/**
 * @author 锟斤拷 <br>
 * 2020/06/25 <br>
 * 字符替换接口
 */
@FunctionalInterface
public interface StringReplacementFunction {
    
    /**
     * 字符替换接口
     * @param group 正则表达式匹配到的捕获组字符串
     * @return 返回需要替换的字符串 如果是null 则保留原来的内容
     */
    String apply(String group);
}
