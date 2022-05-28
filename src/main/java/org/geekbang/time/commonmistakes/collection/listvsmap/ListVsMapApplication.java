package org.geekbang.time.commonmistakes.collection.listvsmap;

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Assert;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/**
 * @description 10-3 比较List和Map
 */
public class ListVsMapApplication {

    public static void main(String[] args) throws InterruptedException {

        int elementCount = 1000000;
        int loopCount = 1000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("listSearch");
        Object list = listSearch(elementCount, loopCount);
        System.out.println(ObjectSizeCalculator.getObjectSize(list));
        stopWatch.stop();
        stopWatch.start("mapSearch");
        Object map = mapSearch(elementCount, loopCount);
        stopWatch.stop();
        System.out.println(ObjectSizeCalculator.getObjectSize(map));
        System.out.println(stopWatch.prettyPrint());
        TimeUnit.HOURS.sleep(1);
    }

    /**
     * @description 初始化 一个具有 elementCount 个订单对象的 ArrayList，
     * 循环 loopCount 次搜索这个 ArrayList，每次随机搜索一个订单号
     */
    private static Object listSearch(int elementCount, int loopCount) {
        List<Order> list = IntStream.rangeClosed(1, elementCount).mapToObj(i -> new Order(i)).collect(Collectors.toList());
        IntStream.rangeClosed(1, loopCount).forEach(i -> {
            int search = ThreadLocalRandom.current().nextInt(elementCount);
            Order result = list.stream().filter(order -> order.getOrderId() == search).findFirst().orElse(null);
            Assert.assertTrue(result != null && result.getOrderId() == search);
        });
        return list;
    }
/**
 * @description 定义另一个 mapSearch 方法，从一个具有 elementCount 个元素的 Map 中循环 loopCount 次查找随机订单号。
 * Map 的 Key 是订单号，Value 是订单对象：
 */
    private static Object mapSearch(int elementCount, int loopCount) {
        //Function.identity()返回一个输出跟输入一样的Lambda表达式对象
        Map<Integer, Order> map = IntStream.rangeClosed(1, elementCount).boxed().collect(Collectors.toMap(Function.identity(), i -> new Order(i)));
        IntStream.rangeClosed(1, loopCount).forEach(i -> {
            int search = ThreadLocalRandom.current().nextInt(elementCount);
            Order result = map.get(search);
            Assert.assertTrue(result != null && result.getOrderId() == search);
        });
        return map;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Order {
        private int orderId;
    }
}

