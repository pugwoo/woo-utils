package com.pugwoo.wooutils.lang;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
	 * 保留decimalPlaces位小数，四舍五入 例如：
	 * 输入 (1.236, 2) 输出1.24
	 * 输入 (1.2, 2) 输出1.20
	 * @param number 注意精度问题 建议使用 {@link #roundUp(BigDecimal, int)}
	 * @param decimalPlaces 保留小数位数
	 * @return
	 */
	public static String roundUp(double number, int decimalPlaces) {
		return roundUp(new BigDecimal(Double.toString(number)), decimalPlaces).toString();
	}
	
	/**
	 * 保留decimalPlaces位小数，例如：
	 * 输入 (1.236, 2) 输出1.24
	 * 输入 (1.2, 2) 输出1.20
	 * @param number 注意精度问题 建议使用 {@link #roundUp(BigDecimal, int)}
	 * @param decimalPlaces 保留小数位数
	 * @return
	 */
	public static double roundUpToDouble(double number, int decimalPlaces) {
		return new Double(roundUp(number, decimalPlaces));
	}
	
	/**
	 * 四舍五入保留decimalPlaces位小数，例如：
	 * 输入 (1.236, 2) 输出1.24
	 * 输入 (1.2, 2) 输出1.20
	 * @param number 数字
	 * @param decimalPlaces 保留的小数点位数
	 */
	public static BigDecimal roundUp(BigDecimal number, int decimalPlaces) {
	    if(number == null) { return null; }
	    return number.setScale(decimalPlaces, RoundingMode.HALF_UP);
	}

	/**
	 * 数值求和
	 * @param list 待计算list item默认可以转为BigDecimal，如果转不了视为0
	 * @return 数据不存在时返回0
	 */
	public static <T> BigDecimal sum(List<T> list) {
		return sum(list, null);
	}
	
	/**
	 * 数值求和
	 * @param list 待计算list
	 * @param mapper 不提供视为item可以转为BigDecimal
	 *               item -> 可以转为BigDecimal，如果转不了视为0
	 * @return 数据不存在时返回0
	 */
	public static <T> BigDecimal sum(List<T> list, Function<? super T, ?> mapper) {
		BigDecimal sum = BigDecimal.ZERO;
		if(list == null || list.isEmpty()) {
			return sum;
		}
		if (mapper == null) {
			mapper = o -> o;
		}
		for(T t : list) {
			if(t == null) {
				continue;
			}
			Object val = mapper.apply(t);
			BigDecimal a = parseBigDecimal(val);
			if(a == null) {
				continue;
			}
			sum = sum.add(a);
		}
		return sum;
	}
	
	/**
	 * 数值求平均值
	 * @param list 待计算的list item默认可以转为BigDecimal，如果转不了视为0
	 * @param decimalPlaces 保留小数点数，四舍五入
	 * @return 数据不存在时返回0
	 */
	public static <T> BigDecimal avg(List<T> list, int decimalPlaces) {
		return avg(list, null, decimalPlaces);
	}
	
	/**
	 * 数值求平均值
	 * @param list   待计算的list
	 * @param mapper item -> 可以转为BigDecimal，如果转不了视为0
	 * @param decimalPlaces 保留小数点数，四舍五入
	 *                      计算平均值出现无限循环小数而不指定保留小数位数会抛ArithmeticException
	 * @return 数据不存在时返回0
	 */
	public static <T> BigDecimal avg(List<T> list, Function<? super T, ?> mapper, int decimalPlaces) {
		if(list == null || list.isEmpty()) {
			return BigDecimal.ZERO;
		}
		if (mapper == null) {
			mapper = o -> o;
		}
		BigDecimal sum = sum(list, mapper);
		return sum.divide(new BigDecimal(list.size()), decimalPlaces, RoundingMode.HALF_UP);
	}
	
}
