package org.geekbang.time.commonmistakes.numeralcalculations.equals;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @description 09-3 equals 方法对两个 BigDecimal 判等
 *
 */
public class CommonMistakesApplication {

    public static void main(String[] args) {
        wrong();
        right();
        set();
    }

    //false
    private static void wrong() {
        System.out.println(new BigDecimal("1.0").equals(new BigDecimal("1")));
    }

    /**
     * @description 只比较 BigDecimal 的 value，可以使用 compareTo 方法
     */
    private static void right() {
        System.out.println(new BigDecimal("1.0").compareTo(new BigDecimal("1")) == 0);
    }

    /**
     * @description 我们把值 为 1.0 的 BigDecimal 加入 HashSet，然后判断其是否存在值为 1 的 BigDecimal
     * BigDecimal 的 equals 和 hashCode 方法在源码中重写的时候同时考虑 value 和 scale，
     * 如果结合 HashSet 或 HashMap 使用的话  1.0和1所产生的hashcode是不一致的
     */
    private static void set() {
        Set<BigDecimal> hashSet1 = new HashSet<>();
        hashSet1.add(new BigDecimal("1.0"));
        System.out.println(hashSet1.contains(new BigDecimal("1")));//返回false

        //存入和比较的时候使用 stripTrailingZeros 方法去掉尾部的零  这样只比较具体的value
        Set<BigDecimal> hashSet2 = new HashSet<>();
        hashSet2.add(new BigDecimal("1.0").stripTrailingZeros());
        System.out.println(hashSet2.contains(new BigDecimal("1.000").stripTrailingZeros()));//返回true

        Set<BigDecimal> treeSet = new TreeSet<>();
        treeSet.add(new BigDecimal("1.0"));
        System.out.println(treeSet.contains(new BigDecimal("1")));//返回true
    }

}

