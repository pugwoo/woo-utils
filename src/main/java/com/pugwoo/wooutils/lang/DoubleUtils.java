package com.pugwoo.wooutils.lang;

import java.text.DecimalFormat;

public class DoubleUtils {
	
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
