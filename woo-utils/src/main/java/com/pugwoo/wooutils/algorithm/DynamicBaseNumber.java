package com.pugwoo.wooutils.algorithm;

/**
 * 不定长进制的数字，只支持表达0或正数
 */
public class DynamicBaseNumber {

    /**
     * 获得指定进制下的0值
     * @param base 进制，有多少位就要提供多少个base，不能省略
     */
    public static int[] getZero(int[] base) {
        return new int[base.length];
    }

    /**
     * 自增1
     * @param base 进制，有多少位就要提供多少个base，不能省略
     * @param number 数字
     */
    public static void increment(int[] base, int[] number) {
        for (int i = base.length - 1; i >= 0; i--) {
            number[i] = number[i] + 1;
            if (number[i] >= base[i]) {
                number[i] = 0;
            } else {
                break; // carry为0，后续的位数不用再计算了
            }
        }
    }

    public static void decrement(int[] base, int[] number) {
        for (int i = base.length - 1; i >= 0; i--) {
            number[i] = number[i] -1;
            if (number[i] < 0) {
                number[i] = base[i] - 1;
            } else {
                break; // carry为0，后续的位数不用再计算了
            }
        }
    }

    /**
     * 判断数字是否是最大值
     */
    public static boolean isMax(int[] base, int[] number) {
        for (int i = 0; i < base.length; i++) {
            if (number[i] != base[i] - 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断数字是否为0
     */
    public static boolean isZero(int[] base, int[] number) {
        for (int i = 0; i < base.length; i++) {
            if (number[i] != 0) {
                return false;
            }
        }
        return true;
    }

    // TODO 支持将一个数字转成指定进制的数字，反过来也是

}
