package com.pugwoo.wooutils.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class NumberUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NumberUtils.class);

	/**
	 * 转换成integer，不会抛出异常，转换失败返回null
	 */
	public static Integer parseInt(Object obj) {
		if(obj == null) {
			return null;
		}
		if(obj instanceof Integer) {
			return (Integer) obj;
		}
		try {
			return Integer.valueOf(obj.toString().trim().replaceAll(",", ""));
		} catch (Exception e) {
			LOGGER.error("parseInt fail, obj:{}", obj, e);
			return null;
		}
	}
	
	/**
	 * 转换成Long，不会抛出异常，转换失败返回null
	 */
	public static Long parseLong(Object obj) {
		if(obj == null) {
			return null;
		}
		if(obj instanceof Long) {
			return (Long) obj;
		}
		try {
			return Long.valueOf(obj.toString().trim().replaceAll(",", ""));
		} catch (Exception e) {
			LOGGER.error("parseLong fail, obj:{}", obj, e);
			return null;
		}
	}
	
	/**
	 * 转换成Double，不会抛出异常，转换失败返回null
	 */
	public static Double parseDouble(Object obj) {
		if(obj == null) {
			return null;
		}
		if(obj instanceof Double) {
			return (Double) obj;
		}
		try {
			return Double.valueOf(obj.toString().trim().replaceAll(",", ""));
		} catch (Exception e) {
			LOGGER.error("parseDouble fail, obj:{}", obj, e);
			return null;
		}
	}
	
	/**
	 * 转换成BigDecimal，不会抛出异常，转换失败返回null
	 */
	public static BigDecimal parseBigDecimal(Object obj) {
		if(obj == null) {
			return null;
		}
		if(obj instanceof BigDecimal) {
			return (BigDecimal) obj;
		}
		try {
			return new BigDecimal(obj.toString().trim().replaceAll(",", ""));
		} catch (Exception e) {
			LOGGER.error("parseBigDecimal fail, obj:{}", obj, e);
			return null;
		}
	}

	/**
	 * 计算百分比
	 */
	public static int percent(Integer num, Integer total) {
		if (num == null || total == null || total == 0) {
			return 0;
		}
		return (int) (num * 100.0 / total);
	}

	/**
	 * 计算百分比
	 */
	public static BigDecimal percent(Integer num, Integer total, Integer scale) {
		if (num == null || total == null || total == 0) {
			return BigDecimal.ZERO;
		}
		return percent(BigDecimal.valueOf(num), BigDecimal.valueOf(total), scale);
	}

	/**
	 * 计算百分比
	 */
	public static int percent(Long num, Long total) {
		if (num == null || total == null || total == 0) {
			return 0;
		}
		return (int) (num * 100.0 / total);
	}

	/**
	 * 计算百分比
	 */
	public static BigDecimal percent(Long num, Long total, Integer scale) {
		if (num == null || total == null || total == 0) {
			return BigDecimal.ZERO;
		}
		return percent(BigDecimal.valueOf(num), BigDecimal.valueOf(total), scale);
	}

	/**
	 * 计算百分比
	 */
	public static int percent(BigDecimal num, BigDecimal total) {
		if (num == null || total == null || total.compareTo(BigDecimal.ZERO) == 0) {
			return 0;
		}

		return (int) (num.doubleValue() * 100.0 / total.doubleValue());
	}

	/**
	 * 计算百分比，保留scale位小数
	 */
	public static BigDecimal percent(BigDecimal num, BigDecimal total, Integer scale) {
		if (num == null || total == null || total.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		num = num.multiply(BigDecimal.valueOf(100));
		return divide(num, total, scale);
	}

	/**
	 * 除法，四舍五入
	 * @param scale 保持N位小数
	 */
	public static BigDecimal divide(BigDecimal a, BigDecimal b, Integer scale) {
		if (scale == null) {
			scale = 0;
		}
		if (a == null || b == null) {
			return null;
		}
		if (b.compareTo(BigDecimal.ZERO) == 0) {
			throw new ArithmeticException("Division by zero");
		}
		return a.divide(b, scale, RoundingMode.HALF_UP);
	}

	/**
	 * 除法，四舍五入
	 * @param scale 保持N位小数
	 */
	public static BigDecimal divide(BigDecimal a, Integer b, Integer scale) {
		if (a == null || b == null) {
			return null;
		}
		if (b == 0) {
			throw new ArithmeticException("Division by zero");
		}
		return divide(a, BigDecimal.valueOf(b), scale);
	}

	/**
	 * 除法，四舍五入
	 * @param scale 保持N位小数
	 */
	public static BigDecimal divide(Integer a, BigDecimal b, Integer scale) {
		if (a == null || b == null) {
			return null;
		}
		if (b.compareTo(BigDecimal.ZERO) == 0) {
			throw new ArithmeticException("Division by zero");
		}
		return divide(BigDecimal.valueOf(a), b, scale);
	}

	/**
	 * 除法，四舍五入
	 * @param scale 保持N位小数
	 */
	public static BigDecimal divide(Integer a, Integer b, Integer scale) {
		if (a == null || b == null) {
			return null;
		}
		if (b == 0) {
			throw new ArithmeticException("Division by zero");
		}
		return divide(BigDecimal.valueOf(a), BigDecimal.valueOf(b), scale);
	}

	/**
	 * 保留decimalPlaces位小数，四舍五入，返回字符串形式。<br>
	 * 例如：<br>
	 * 输入 (1.236, 2) 输出 "1.24" <br>
	 * 输入 (1.2, 2) 输出 "1.20" <br>
	 * <br>
	 * 注意：double类型存在精度问题，如果需要精确计算，请直接使用 {@link #roundUp(BigDecimal, int)} 方法。
	 * @param number 数字
	 * @param decimalPlaces 保留小数位数
	 */
	public static String roundUp(double number, int decimalPlaces) {
		return roundUp(new BigDecimal(Double.toString(number)), decimalPlaces).toString();
	}
	
	/**
	 * 保留decimalPlaces位小数，四舍五入，返回double形式。<br>
	 * 例如：<br>
	 * 输入 (1.236, 2) 输出 1.24 <br>
	 * 输入 (1.2, 2) 输出 1.20 <br>
	 * <br>
	 * 注意：double类型存在精度问题，如果需要精确计算，请直接使用 {@link #roundUp(BigDecimal, int)} 方法。
	 * @param number 数字
	 * @param decimalPlaces 保留小数位数
	 */
	public static double roundUpToDouble(double number, int decimalPlaces) {
		return Double.parseDouble(roundUp(number, decimalPlaces));
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

	public static BigDecimal max(BigDecimal a, BigDecimal b) {
		if (a == null) {
			return b;
		} else if (b == null) {
			return a;
		}
		return a.compareTo(b) >= 0 ? a : b;
	}

	/**
	 * 返回两个数中的较大者，如果一个为null则返回非null的那个
	 * @param a 第一个数
	 * @param b 第二个数
	 * @return 较大者
	 */
	public static <T extends Comparable<? super T>> T max(T a, T b) {
		if (a == null) {
			return b;
		} else if (b == null) {
			return a;
		}
		return a.compareTo(b) >= 0 ? a : b;
	}

	public static BigDecimal min(BigDecimal a, BigDecimal b) {
		if (a == null) {
			return b;
		} else if (b == null) {
			return a;
		}
		return a.compareTo(b) > 0 ? b : a;
	}

    /**
     * 返回两个数中的较小者，如果一个为null则返回非null的那个
     * @param a 第一个数
     * @param b 第二个数
     * @return 较小者
     */
    public static <T extends Comparable<? super T>> T min(T a, T b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }
        return a.compareTo(b) > 0 ? b : a;
    }

	/**
	 * 求最小值
	 * @param list 待计算list
	 * @param mapper 转换成比较类型
	 * @return 不存在时返回null
	 */
	public static <T, R extends Comparable<? super R>> R min(List<T> list,
													 Function<? super T, R> mapper) {
		if (list == null || list.isEmpty()) {
			return null;
		}

		R min = null;
		for (T t : list) {
			if (t == null) {
				continue;
			}
			R r = mapper.apply(t);
			if (r == null) {
				continue;
			}
			if (min == null || r.compareTo(min) < 0) {
				min = r;
			}
		}
		return min;
	}

	/**
	 * 求最大值
	 * @param list 待计算list
	 * @param mapper 转换成比较类型
	 * @return 不存在时返回null
	 */
	public static <T, R extends Comparable<? super R>> R max(List<T> list,
													 Function<? super T, R> mapper) {
		if (list == null || list.isEmpty()) {
			return null;
		}

		R max = null;
		for (T t : list) {
			if (t == null) {
				continue;
			}
			R r = mapper.apply(t);
			if (r == null) {
				continue;
			}
			if (max == null || r.compareTo(max) > 0) {
				max = r;
			}
		}
		return max;
	}

	/**
	 * 数值求和
	 * @param list 待计算list item默认可以转为BigDecimal，如果转不了视为0
	 * @return 数据不存在时返回0
	 */
	public static <T> BigDecimal sum(Collection<T> list) {
		return sum(list, o -> o);
	}
	
	/**
	 * 数值求和
	 * @param list 待计算list
	 * @param mapper 不提供视为item可以转为BigDecimal
	 *               item 可以转为BigDecimal，如果转不了视为0
	 * @return 数据不存在时返回0
	 */
	public static <T> BigDecimal sum(Collection<T> list, Function<? super T, ?> mapper) {
		if (mapper == null) {
			throw new RuntimeException("mapper can not be null");
		}

		BigDecimal sum = BigDecimal.ZERO;
		if(list == null || list.isEmpty()) {
			return sum;
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
	public static <T> BigDecimal avg(Collection<T> list, int decimalPlaces) {
		return avg(list, decimalPlaces, null);
	}

	/**
	 * 数值求平均值
	 * @param list   待计算的list
	 * @param mapper item 可以转为BigDecimal，如果转不了视为0
	 * @param decimalPlaces 保留小数点数，四舍五入
	 *                      计算平均值出现无限循环小数而不指定保留小数位数会抛ArithmeticException
	 * @return 数据不存在时返回0
	 */
	public static <T> BigDecimal avg(Collection<T> list, int decimalPlaces, Function<? super T, ?> mapper) {
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