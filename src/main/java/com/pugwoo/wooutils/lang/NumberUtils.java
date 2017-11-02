package com.pugwoo.wooutils.lang;

import java.text.DecimalFormat;

public class NumberUtils {
	
	/**
	 * 转换成integer，不会抛出异常，转换失败返回null
	 * @return
	 */
	public static Integer parseInt(Object obj) {
		if(obj == null) {
			return null;
		}
		if(obj instanceof Integer) {
			return (Integer) obj;
		}
		try {
			return new Integer(obj.toString());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 转换成Long，不会抛出异常，转换失败返回null
	 * @return
	 */
	public static Long parseLong(Object obj) {
		if(obj == null) {
			return null;
		}
		if(obj instanceof Long) {
			return (Long) obj;
		}
		try {
			return new Long(obj.toString());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 转换成Double，不会抛出异常，转换失败返回null
	 * @return
	 */
	public static Double parseDouble(Object obj) {
		if(obj == null) {
			return null;
		}
		if(obj instanceof Double) {
			return (Double) obj;
		}
		try {
			return new Double(obj.toString());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 保留decimalPlaces位小数，例如：
	 * 输入 (1.236, 2) 输出1.24
	 * 输入 (1.2, 2) 输出1.2
	 * @param number
	 * @param decimalPlaces
	 * @return
	 */
	public static String roundUp(double number, int decimalPlaces) {
		StringBuilder format = new StringBuilder("#");
		if(decimalPlaces > 0) {
			format.append(".");
		}
		for(int i = 0; i < decimalPlaces; i++) {
			format.append("#");
		}
		DecimalFormat df = new DecimalFormat(format.toString());
		return df.format(number);
	}
	
	/**
	 * 保留decimalPlaces位小数，例如：
	 * 输入 (1.236, 2) 输出1.24
	 * 输入 (1.2, 2) 输出1.2
	 * @param number
	 * @param decimalPlaces
	 * @return
	 */
	public static double roundUpToDouble(double number, int decimalPlaces) {
		return new Double(roundUp(number, decimalPlaces));
	}
}
