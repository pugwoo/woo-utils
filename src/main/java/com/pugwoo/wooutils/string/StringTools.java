package com.pugwoo.wooutils.string;

import java.util.Random;

/**
 * 2016年2月4日 11:29:00
 * 字符串工具类
 */
public class StringTools {
	
	/**
	 * 把obj转换成string，如果obj为null，返回null
	 * @param obj
	 * @return
	 */
	public static String toString(Object obj) {
		return obj == null ? null : obj.toString();
	}
	
	/**
	 * 把obj转换成string，如果obj为null，返回参数ifnull的值
	 * @param obj
	 * @param ifnull
	 * @return
	 */
	public static String toString(Object obj, String ifnull) {
		return obj == null ? ifnull : obj.toString();
	}
	
	/**
	 * 生成随机字符串，例如输出source=0123456789,num=6，输出就是6位随机数字
	 * @param source 字符串的输入源，例如纯数字的话，这里输入0123456789
	 * @param num 生成出几位的string
	 * @return
	 */
	public static String randomString(String source, int num) {
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for(int i = 0; i < num; i++) {
			sb.append(source.charAt(random.nextInt(source.length())));
		}
		return sb.toString();
	}
	
	/**
	 * 将字符串str拆分成一行一个字符串
	 * @param str
	 * @return
	 */
	public static String[] splitLines(String str) {
		if(str == null) {
			return new String[0];
		}
		return str.split("\\r?\\n");
	}
	
	/**
	 * 将字符串str按空行拆分成不同的字符串
	 * @param str
	 * @return
	 */
	public static String[] splitByEmptyLines(String str) {
		if(str == null) {
			return new String[0];
		}
		return str.split("\\r?\\n[\\r?\\n]+");
	}
	
	/**
	 * 判定字符串是否只包含英文字母(不区分大小写)或数字
	 * @param str 不能为null
	 * @return 空字符串时返回true
	 */
    public static boolean isEnglishLetterOrNumeric(String str) {
    	int length = str.length();
    	for(int i = 0; i < length; i++) {
    		char c = str.charAt(i);
    		if(!(c>='a' && c<='z' || c>='A' && c<='Z' || c>='0' && c<='9')) {
    			return false;
    		}
    	}
    	return true;
    }
	
    /**
     * 数字转换成中文大写钱币，精确到分，四舍五入；最大9万亿9。
     */
    public static String moneyToChineseUpper(double n) {
        String UNIT = "万千佰拾亿千佰拾万千佰拾元角分";
        String DIGIT = "零壹贰叁肆伍陆柒捌玖";
        double MAX_VALUE = 9999999999999.99D;

        if (n < 0 || n > MAX_VALUE) return "参数非法!";
        long l = Math.round(n * 100);
        if (l == 0) return "零元整";
        String strValue = l + "";
        // i用来控制数
        int i = 0;
        // j用来控制单位
        int j = UNIT.length() - strValue.length();
        StringBuilder rs = new StringBuilder();
        boolean isZero = false;
        for (; i < strValue.length(); i++, j++) {
            char ch = strValue.charAt(i);

            if (ch == '0') {
                isZero = true;
                if (UNIT.charAt(j) == '亿' || UNIT.charAt(j) == '万' || UNIT.charAt(j) == '元') {
                    rs.append(UNIT.charAt(j));
                    isZero = false;
                }
            } else {
                if (isZero) {
                    rs.append("零");
                    isZero = false;
                }
                rs = rs.append(DIGIT.charAt(ch - '0')).append(UNIT.charAt(j));
            }
        }

        if (!rs.toString().endsWith("分")) {
            rs = rs.append("整");
        }
        String ret = rs.toString();
        ret = ret.replaceAll("亿万", "亿");
        return ret;
    }
}
