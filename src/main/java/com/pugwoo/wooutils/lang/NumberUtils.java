package com.pugwoo.wooutils.lang;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Function;

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
			return new Integer(obj.toString().trim());
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
			return new Long(obj.toString().trim());
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
			return new Double(obj.toString().trim());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 转换成BigDecimal，不会抛出异常，转换失败返回null
	 * @param obj
	 * @return
	 */
	public static BigDecimal parseBigDecimal(Object obj) {
		if(obj == null) {
			return null;
		}
		if(obj instanceof BigDecimal) {
			return (BigDecimal) obj;
		}
		try {
			return new BigDecimal(obj.toString().trim());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 保留decimalPlaces位小数，例如：
	 * 输入 (1.236, 2) 输出1.24
	 * 输入 (1.2, 2) 输出1.20
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
			format.append("0");
		}
		DecimalFormat df = new DecimalFormat(format.toString());
		return df.format(number);
	}
	
	/**
	 * 保留decimalPlaces位小数，例如：
	 * 输入 (1.236, 2) 输出1.24
	 * 输入 (1.2, 2) 输出1.20
	 * @param number
	 * @param decimalPlaces
	 * @return
	 */
	public static double roundUpToDouble(double number, int decimalPlaces) {
		return new Double(roundUp(number, decimalPlaces));
	}
	
	/**
	 * 保留decimalPlaces位小数，例如：
	 * 输入 (1.236, 2) 输出1.24
	 * 输入 (1.2, 2) 输出1.20
	 * @param number
	 * @param decimalPlaces
	 * @return
	 */
	public static BigDecimal roundUp(BigDecimal number, int decimalPlaces) {
	    if(number == null) return null;
	    return number.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * 数值求和
	 * @param list
	 * @param mapper
	 * @return
	 */
	public static <T> BigDecimal sum(List<T> list, Function<? super T, ?> mapper) {
		BigDecimal sum = BigDecimal.ZERO;
		if(list == null || list.isEmpty()) {
			return sum;
		}
		for(T t : list) {
			if(t == null) {
				continue;
			}
			Object val = mapper.apply(t);
			if(val == null) {
				continue;
			}

			BigDecimal a = null;
			if(!(val instanceof BigDecimal)) {
				try {
					a = new BigDecimal(val.toString());
				} catch (Exception e) { // ignore
				}
			} else {
				a = (BigDecimal) val;
			}

			if(a == null) {
				continue;
			}
			sum = sum.add(a);
		}
		return sum;
	}

	/**
	 * 数值求平均值
	 * @param list
	 * @param mapper
	 * @param decimalPlaces 保留小数点数，四舍五入
	 * @return 数据不存在时返回0
	 */
	public static <T> BigDecimal avg(List<T> list, Function<? super T, ?> mapper, int decimalPlaces) {
		if(list == null || list.isEmpty()) {
			return BigDecimal.ZERO;
		}

		BigDecimal sum = sum(list, mapper);
		return sum.divide(new BigDecimal(list.size()),
				new MathContext(decimalPlaces, RoundingMode.HALF_UP));
	}

	/**
	 * 数值求平均值
	 * @param list
	 * @param mapper
	 * @return 数据不存在时返回0
	 */
	public static <T> BigDecimal avg(List<T> list, Function<? super T, ?> mapper) {
		if(list == null || list.isEmpty()) {
			return BigDecimal.ZERO;
		}

		BigDecimal sum = sum(list, mapper);
		return sum.divide(new BigDecimal(list.size()));
	}

}
