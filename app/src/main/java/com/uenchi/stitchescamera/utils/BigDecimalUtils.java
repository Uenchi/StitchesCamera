package com.uenchi.stitchescamera.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author : uenchi
 * @date : 2021/4/12
 * @desc :
 */
public class BigDecimalUtils {
    /**
     * 保留一位小数
     *
     * @param x
     * @return
     */
    public static double decimalOne(double x) {
        double a = BigDecimalUtils.multiply(x, 1);
        int b = (int) BigDecimalUtils.multiply(a, 10);

        return BigDecimalUtils.divide(b, 10);
    }

    /**
     * 保留两位小数
     *
     * @param x
     * @return
     */
    public static double decimalTwo(double x) {
        double a = BigDecimalUtils.multiply(x, 1);
        int b = (int) BigDecimalUtils.multiply(a, 100);

        return BigDecimalUtils.divide(b, 100);
    }

    /**
     * 加法运算
     *
     * @param x
     * @param y
     * @return
     */
    public static double add(double x, double y) {
        BigDecimal bigX = new BigDecimal(Double.toString(x));
        BigDecimal bigY = new BigDecimal(Double.toString(y));
        return bigX.add(bigY).doubleValue();
    }

    /**
     * 加法运算
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static double add(double x, double y, double z) {
        BigDecimal bigX = new BigDecimal(Double.toString(x));
        BigDecimal bigY = new BigDecimal(Double.toString(y));
        BigDecimal bigZ = new BigDecimal(Double.toString(z));
        return bigX.add(bigY).add(bigZ).doubleValue();
    }

    /**
     * 减法运算
     *
     * @param x
     * @param y
     * @return
     */
    public static double subtract(double x, double y) {
        BigDecimal bigX = new BigDecimal(Double.toString(x));
        BigDecimal bigY = new BigDecimal(Double.toString(y));
        return bigX.subtract(bigY).doubleValue();
    }

    /**
     * 减法运算
     *
     * @param x
     * @param y
     * @return
     */
    public static double sub(double x, double y) {
        return subtract(x, y);
    }

    /**
     * 乘法运算
     *
     * @param x
     * @param y
     * @return
     */
    public static double multiply(double x, double y) {
        BigDecimal bigX = new BigDecimal(Double.toString(x));
        BigDecimal bigY = new BigDecimal(Double.toString(y));
        return bigX.multiply(bigY).doubleValue();
    }

    public static long multiplyLong(long x, long y) {
        BigDecimal bigX = new BigDecimal(Double.toString(x));
        BigDecimal bigY = new BigDecimal(Double.toString(y));
        return bigX.multiply(bigY).longValue();
    }

    /**
     * 乘法运算
     *
     * @param x
     * @param y
     * @param decimalFormat 保留小数点位数#0.00
     * @return
     */
    public static String multiply(double x, double y, String decimalFormat) {
        return new DecimalFormat(decimalFormat).format(multiply(x, y));
    }

    /**
     * 乘法运算
     *
     * @param x
     * @param y
     * @return
     */
    public static double mul(double x, double y) {
        return multiply(x, y);
    }

    /**
     * 乘法运算
     * 保留小数点位数#0.00
     *
     * @param x
     * @param y
     * @return
     */
    public static double multiplication(double x, double y) {
        double a = BigDecimalUtils.multiply(x, y);
        int b = (int) BigDecimalUtils.multiply(a, 100);

        return BigDecimalUtils.divide(b, 100);
    }


    /**
     * 除法运算
     *
     * @param x
     * @param y
     * @return
     */
    public static double divide(double x, double y) {
        if (y == 0) {
            System.err.println("错误：被除数不能为0！");
            System.exit(-1);
        }
        BigDecimal bigX = new BigDecimal(Double.toString(x));
        BigDecimal bigY = new BigDecimal(Double.toString(y));
        return bigX.divide(bigY, 2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }

    /**
     * 除法运算
     * 保留小数点位数#0.00
     *
     * @param x
     * @param y
     * @return
     */
    public static double division(double x, double y) {
        if (y == 0) {
            System.err.println("错误：被除数不能为0！");
            System.exit(-1);
        }
        BigDecimal bigX = new BigDecimal(Double.toString(x));
        BigDecimal bigY = new BigDecimal(Double.toString(y));
        double a = bigX.divide(bigY, 2, BigDecimal.ROUND_FLOOR).doubleValue();

        int b = (int) BigDecimalUtils.multiply(a, 100);

        return BigDecimalUtils.divide(b, 100);
    }

    /**
     * 清除多余的0 例如：1.00 返回 1
     *
     * @return
     */
    public static String cleanSurplusZero(String num) {
        if (EmptyUtils.isEmpty(num)) return "";
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(Float.valueOf(num));
    }

//    public static DoubleLeaveBean leave(String num) {
//        if (EmptyUtils.isEmpty(num)) return new DoubleLeaveBean("", "");
//
//        int index = num.indexOf(".");
//        String h;
//        String l;
//
//        if (index != -1) {
//            h = num.substring(0, index);
//            l = num.substring(index, num.length());
//        } else {
//            h = num;
//            l = "";
//        }
//
//        return new DoubleLeaveBean(h, l);
//    }
}
