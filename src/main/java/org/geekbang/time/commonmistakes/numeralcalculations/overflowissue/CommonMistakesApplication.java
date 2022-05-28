package org.geekbang.time.commonmistakes.numeralcalculations.overflowissue;

import java.math.BigInteger;
/**
 * @description 09-4  数值溢出问题 溢出是没有异常的
 */
public class CommonMistakesApplication {

    public static void main(String[] args) {

        System.out.println("wrong");
        wrong();
        System.out.println("right1");
        right1();
        System.out.println("right2");
        right2();
    }

    private static void wrong() {
        long l = Long.MAX_VALUE;
        System.out.println(l + 1);
        System.out.println(l + 1 == Long.MIN_VALUE);
    }

    /**
     * @description Math 类的 addExact、subtractExact 等 xxExact 方法进行数值运 算，
     * 这些方法可以在数值溢出时主动抛出异常
     */
    private static void right2() {
        try {
            long l = Long.MAX_VALUE;
            System.out.println(Math.addExact(l, 1));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @description BigInteger处理大数
     */
    private static void right1() {

        BigInteger i = new BigInteger(String.valueOf(Long.MAX_VALUE));
        System.out.println(i.add(BigInteger.ONE).toString());

        try {
            long l = i.add(BigInteger.ONE).longValueExact();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

