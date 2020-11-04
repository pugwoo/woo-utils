package com.pugwoo.wooutils.string;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * 2016年2月4日 11:29:00
 * 字符串工具类
 */
public class StringTools {
	
	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}
	
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}
	
	public static boolean isBlank(String str) {
        if(isEmpty(str)) return true;
        for(char c : str.toCharArray()) {
			if (!Character.isWhitespace(c)) {
				return false;
			}
		}
        return true;
	}

	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}
	
	/**
	 * 判断字符串str是否在strSet列表中
	 * @param str
	 * @param strSet
	 * @return
	 */
	public static boolean isIn(String str, String... strSet) {
		if(strSet == null) return false;
		for(String s : strSet) {
			if(Objects.equals(str, s)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断字符串str是否在strSet列表中,不区分大小写
	 * @param str
	 * @param strSet
	 * @return
	 */
	public static boolean isInIgnoreCase(String str, String... strSet) {
		if(strSet == null) return false;
		for(String s : strSet) {
			if(str == null && s == null) {
				return true;
			}
			if(str != null && str.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

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
	 * 将list的元素的toString 用分隔符splitLetter连起来。<br>
	 * 如果list中元素等于null或者toString为null或空字符串，则不加入。<br>
	 *
	 * 示例：输入list=[1,2,3],splitLetter=;，则输出1;2;3
	 *
	 * @param splitLetter
	 * @param list
	 * @return 不会返回null
	 */
	public static String join(List<?> list, String splitLetter) {
		if (list == null || list.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		boolean isEmpty = true;
		for(Object obj : list) {
			if(obj == null) {
				continue;
			}
			String objStr = obj.toString();
			if(isEmpty(objStr)) {
				continue;
			}

			if(!isEmpty) {
				sb.append(splitLetter);
			}
			sb.append(objStr);
			isEmpty = false;
		}
		return sb.toString();
	}

	/**
	 * 将数组的元素的toString 用分隔符splitLetter连起来。<br>
	 * 如果数组中元素等于null或者toString为null或空字符串，则不加入。<br>
	 *
	 * 示例：输入array=[1,2,3],splitLetter=;，则输出1;2;3
	 *
	 * @param splitLetter
	 * @param array
	 * @return 不会返回null
	 */
	public static String join(Object[] array, String splitLetter) {
		if (array == null || array.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		boolean isEmpty = true;
		for(Object obj : array) {
			if(obj == null) {
				continue;
			}
			String objStr = obj.toString();
			if(isEmpty(objStr)) {
				continue;
			}

			if(!isEmpty) {
				sb.append(splitLetter);
			}
			sb.append(objStr);
			isEmpty = false;
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
	 * @param str
	 * @return str为null或空字符串返回false
	 */
    public static boolean isAlphabeticOrDigit(String str) {
		if(str == null || str.isEmpty()) {return false;}
		for(char c : str.toCharArray()) {
			if(!(Character.isDigit(c) || Character.isAlphabetic(c))) {
				return false;
			}
		}
		return true;
    }

	/**
	 * 判断str是否全为数字，不包括+-.
	 * @param str
	 * @return str为null或空字符串返回false
	 */
	public static boolean isDigit(String str) {
    	if(str == null || str.isEmpty()) {return false;}
		for(char c : str.toCharArray()) {
			if(!Character.isDigit(c)) {
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
