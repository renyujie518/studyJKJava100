package org.geekbang.time.commonmistakes.advancedfeatures.genericandinheritance;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description 18-2 使用反射查询类方法清单
 */
public class GenericAndInheritanceApplication {

    public static void main(String[] args) {
        wrong3();
    }

    public static void wrong1() {
        Child1 child1 = new Child1();
        //子类方法的调用是通过反射进行的  getMethods返回是个数组
        Arrays.stream(child1.getClass().getMethods())
                .filter(method -> method.getName().equals("setValue"))
                .forEach(method -> {
                    try {
                        method.invoke(child1, "test");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        System.out.println(child1.toString());
    }


    /**
     * @description getMethods 方法能获得当前类和父类的所有 public 方法，
     * getDeclaredMethods 只能获得当前类所有的 public、protected、package 和 private 方法。
     */
    public static void wrong2() {
        Child1 child1 = new Child1();
        Arrays.stream(child1.getClass().getDeclaredMethods())
                .filter(method -> method.getName().equals("setValue"))
                .forEach(method -> {
                    try {
                        method.invoke(child1, "test");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        System.out.println(child1.toString());
    }

    public static void wrong3() {
        Child2 child2 = new Child2();
        Arrays.stream(child2.getClass().getDeclaredMethods())
                .filter(method -> method.getName().equals("setValue"))
                .forEach(method -> {
                    try {
                        method.invoke(child2, "test");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        System.out.println(child2.toString());
    }

    /**
     * @description  Child2 类其实有 2 个 setValue 方法，入参分别是 String 和 Object。
     * 由于编译器采用桥接，在使用时根据方法名 setValue 和 非 isBridge 两个条件过滤，才能实现唯一过滤
     * 只匹配 0 或 1 项的话，可以考虑配合 ifPresent 来使用 findFirst 方法。
     * https://blog.csdn.net/weixin_41888813/article/details/82885938
     * （如果存在一个值，isPresent()将返回true, findFirst即返回列表中的第一个元素）
     */
    public static void right() {
        Child2 child2 = new Child2();
        Arrays.stream(child2.getClass().getDeclaredMethods())
                .filter(method -> method.getName().equals("setValue") && !method.isBridge())
                .findFirst().ifPresent(method -> {
            try {
                method.invoke(child2, "test");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println(child2.toString());
    }
}


/**
 * @description 在类字段内容变动时记录日志
 * 定义一个泛型父类，并 在父类中定义一个统一的日志记录方法，子类可以通过继承重用这个方法
 */
class Parent<T> {

    AtomicInteger updateCount = new AtomicInteger();

    private T value;

    @Override
    public String toString() {
        return String.format("value: %s updateCount: %d", value, updateCount.get());
    }

    //setValue 方法每次为 value 赋值时对计 数器进行 +1 操作
    public void setValue(T value) {
        System.out.println("Parent.setValue called");
        this.value = value;
        updateCount.incrementAndGet();
    }
}

/**
 * @description 继承父类，但没有提供父类泛型参数
 */
class Child1 extends Parent {


    //覆盖父类的 setValue 实现
    public void setValue(String value) {
        System.out.println("Child1.setValue called");
        super.setValue(value);
    }
}

/**
 * @description 继承 Parent 的时候提供了 String 作为泛型 T 类型，并使用 @Override 关键字注释了 setValue 方 法
 */
class Child2 extends Parent<String> {

    @Override
    public void setValue(String value) {
        System.out.println("Child2.setValue called");
        super.setValue(value);
    }
}