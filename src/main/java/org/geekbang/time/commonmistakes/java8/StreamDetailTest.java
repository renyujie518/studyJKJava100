package org.geekbang.time.commonmistakes.java8;


import org.geekbang.time.commonmistakes.java8.collector.MostPopularCollector;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StreamDetailTest {
    private static Random random = new Random();
    private List<Order> orders;

    @Before
    public void data() {
        orders = Order.getData();

        orders.forEach(System.out::println);
        System.out.println("==========================================");
    }

    /**
     * @description filter 方法可以实现过滤操作
     */
    @Test
    public void filter() {
        System.out.println("最近半年的金额大于40的订单");
        orders.stream()
                .filter(Objects::nonNull) //过滤null值
                .filter(order -> order.getPlacedAt().isAfter(LocalDateTime.now().minusMonths(6)))
                .filter(order -> order.getTotalPrice() > 40)
                .forEach(System.out::println);
    }

    /**
     * @description map 操作可以做转换（或者说投影
     */
    @Test
    public void map() {
        //统计所有订单商品数量
        //通过两次遍历实现
        LongAdder longAdder = new LongAdder();
        orders.stream().forEach(order ->
                order.getOrderItemList().forEach(orderItem -> longAdder.add(orderItem.getProductQuantity())));

        //使用两次mapToLong+sum方法实现
        assertThat(longAdder.longValue(), is(
                orders.stream().mapToLong(order ->
                order.getOrderItemList().stream()
                        .mapToLong(OrderItem::getProductQuantity).sum()).sum()));

        //把IntStream通过转换Stream<Project>
        System.out.println("替代 for 循环");
        System.out.println(IntStream.rangeClosed(1, 10)
                .mapToObj(i -> new Product((long) i, "product" + i, i * 100.0))
                .collect(toList()));
    }

    /**
     * @description 实现大于 50 元 订单的按价格倒序取前 5
     * 可以通过 Order::getTotalPrice 方法引用直接指定需要排序的 依据字段，通过 reversed() 实现倒序
     */
    @Test
    public void sorted() {
        System.out.println("//大于50的订单,按照订单价格倒序前5");
        orders.stream().filter(order -> order.getTotalPrice() > 50)
                .sorted(comparing(Order::getTotalPrice).reversed())
                .limit(5)
                .forEach(System.out::println);
    }

    /**
     * @description latMap 展开或者叫扁平化操作，相当于 map+flat，通过 map 把每一 个元素替换为一个流，然后展开这个流。
     * 统计所有订单的总价格
     */
    @Test
    public void flatMap() {
        //不依赖订单上的总价格字段
        System.out.println(orders.stream().mapToDouble(order -> order.getTotalPrice()).sum());

        //如果不依赖订单上的总价格,可以直接展开订单商品进行价格统计
        /** 通过 flatMap 展 开成商品清单，也就是把 Order 替换为 Stream
            然后对每一个 OrderItem 用 mapToDouble 转换获得商品总价，最后进行一次 sum 求和**/
        System.out.println(orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .mapToDouble(item -> item.getProductQuantity() * item.getProductPrice()).sum());

        //另一种方式flatMap+mapToDouble=flatMapToDouble
        /** 利用 flatMapToDouble 方法把列表中每一项展开替换为一个 DoubleStream
         *  也就是 直接把每一个订单转换为每一个商品的总价，然后求和 **/
        System.out.println(orders.stream()
                .flatMapToDouble(order -> order.getOrderItemList().stream()
                        .mapToDouble(item -> item.getProductQuantity() * item.getProductPrice())).sum());
    }

    @Test
    public void groupBy() {
        System.out.println("//按照用户名分组，统计每个人的下单数 量，再按照下单数量倒序");
        System.out.println(orders.stream().collect(groupingBy(Order::getCustomerName, counting()))//这里相当key value
                .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).collect(toList()));

        System.out.println("//按照用户名分组,统计订单总金额,再按照总金额倒序");
        System.out.println(orders.stream().collect(groupingBy(Order::getCustomerName, summingDouble(Order::getTotalPrice)))
                .entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()).collect(toList()));

        System.out.println("//按照用户名分组，使用两次 Collectors.summingInt 方法统计商品采购数 量，再按总数量倒序输出");
        System.out.println(orders.stream().collect(groupingBy(Order::getCustomerName,
                summingInt(order -> order.getOrderItemList().stream()
                        .collect(summingInt(OrderItem::getProductQuantity)))))
                .entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(toList()));

        System.out.println("//统计最受欢迎的商品（采购最多），倒序后取第一个");
        orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .collect(groupingBy(OrderItem::getProductName, summingInt(OrderItem::getProductQuantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresent(System.out::println);

        System.out.println("//统计最受欢迎的商品的另一种方式,直接利用maxBy  就不用sort再reversed");
        orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .collect(groupingBy(OrderItem::getProductName, summingInt(OrderItem::getProductQuantity)))
                .entrySet().stream()
                .collect(maxBy(Map.Entry.comparingByValue()))
                .map(Map.Entry::getKey)
                .ifPresent(System.out::println);


        System.out.println("//按照用户名分组，选用户下的总金额最大的订单" +
                "Key 是用户名，Value 是 Order，直接通过 Collectors.maxBy 方法拿到金额最高的订单" +
                "然后通过 collectingAndThen 实现 Optional.get 的内容提取，最后遍历 Key/Value 即可。");
        orders.stream().collect(groupingBy(Order::getCustomerName, collectingAndThen(maxBy(comparingDouble(Order::getTotalPrice)), Optional::get)))
                .forEach((k, v) -> System.out.println(k + "#" + v.getTotalPrice() + "@" + v.getPlacedAt()));


        System.out.println("//根据下单年月分组统计订单ID列表" +
                "Value 直接通过 Collectors.mapping 方法进行了转换，把订单列表转换为订单 ID 构成 的 List。");
        System.out.println(orders.stream().collect
                (groupingBy(order -> order.getPlacedAt().format(DateTimeFormatter.ofPattern("yyyyMM")),
                        mapping(order -> order.getId(), toList()))));

        System.out.println("//根据下单年月+用户名两次分组，统计订单ID列表" +
                "Value 直接通过 Collectors.mapping 方法进行了转换，把订单列表转换为订单 ID 构成 的 List。");
        System.out.println(orders.stream().collect
                (groupingBy(order -> order.getPlacedAt().format(DateTimeFormatter.ofPattern("yyyyMM")),
                        groupingBy(order -> order.getCustomerName(),
                                mapping(order -> order.getId(), toList())))));
    }

    @Test
    public void maxMin() {
        orders.stream().max(comparing(Order::getTotalPrice)).ifPresent(System.out::println);
        orders.stream().min(comparing(Order::getTotalPrice)).ifPresent(System.out::println);
    }

    @Test
    public void reduce() {
        System.out.println("//统计花钱最多的人");
        System.out.println(orders.stream().collect(groupingBy(Order::getCustomerName, summingDouble(Order::getTotalPrice)))
                .entrySet().stream()
                .reduce(BinaryOperator.maxBy(Map.Entry.comparingByValue()))
                .map(Map.Entry::getKey).orElse("N/A"));
    }

    @Test
    public void distinct() {
        System.out.println("//不去重的下单用户");
        System.out.println(orders.stream().map(order -> order.getCustomerName()).collect(joining(",")));

        System.out.println("//去重的下单用户");
        System.out.println(orders.stream().map(order -> order.getCustomerName()).distinct().collect(joining(",")));

        System.out.println("//所有购买过的商品并去重");
        System.out.println(orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .map(OrderItem::getProductName)
                .distinct().collect(joining(",")));
    }

    @Test
    public void collect() {
        System.out.println("//生成一定位数的随机字符串");
        System.out.println(random.ints(48, 122)
                .filter(i -> (i < 57 || i > 65) && (i < 90 || i > 97))
                .mapToObj(i -> (char) i)
                .limit(20)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString());

        System.out.println("//所有下单的用户,使用toSet去重了");
        System.out.println(orders.stream()
                .map(order -> order.getCustomerName()).collect(toSet())
                .stream().collect(joining(",", "[", "]")));

        System.out.println("//用toCollection收集器指定集合类型");
        System.out.println(orders.stream().limit(2).collect(toCollection(LinkedList::new)).getClass());

        System.out.println("//使用toMap获取订单ID+下单用户名的Map");
        orders.stream()
                .collect(toMap(Order::getId, Order::getCustomerName))
                .entrySet().forEach(System.out::println);

        System.out.println("//使用toMap获取下单用户名+最近一次下单时间的Map" +
                "(Value 是下单时间，一个用户可能多次下单，所以直接在这里进行了合并，只获取 最近一次的下单时间)");
        orders.stream()
                .collect(toMap(Order::getCustomerName, Order::getPlacedAt, (x, y) -> x.isAfter(y) ? x : y))
                .entrySet().forEach(System.out::println);

        System.out.println("//所有订单平均购买的商品数量");
        System.out.println(orders.stream().collect(averagingInt(
                order -> order.getOrderItemList().stream().collect(summingInt(OrderItem::getProductQuantity)))));
    }

    /**
     * @description partitioningBy 用于分区，分区是特殊的分组，只有 true 和 false 两组
     * 可以把用户分为下过订单和没下过订单两 组：
     */
    @Test
    public void partition() {
        //先来看一下所有下单的用户
        orders.stream().map(order -> order.getCustomerName()).collect(toSet()).forEach(System.out::println);
        //用户按照是否下单进行分区  输出是 Map<Boolean, List>：
        System.out.println(Customer.getData().stream().collect(
                partitioningBy(customer -> orders.stream().mapToLong(Order::getCustomerId)
                        .anyMatch(id -> id == customer.getId()))));
    }

    @Test
    public void skipLimit() {
        //按照下单时间排序(正序)，查询前2个订单的顾客姓名和下单时间
        orders.stream()
                .sorted(comparing(Order::getPlacedAt))
                .map(order -> order.getCustomerName() + "@" + order.getPlacedAt())
                .limit(2).forEach(System.out::println);

        //按照下单时间排序，查询第 3 和第 4 个订单的顾客姓名和下单时间。
        orders.stream()
                .sorted(comparing(Order::getPlacedAt))
                .map(order -> order.getCustomerName() + "@" + order.getPlacedAt())
                .skip(2).limit(2).forEach(System.out::println);
    }

    /**
     * @description peek 方法可以接收一个 consumer 来打印数据，可以接在任意 transfo rmation 操作后面查看数据
     */
    @Test
    public void peek() {
        IntStream.rangeClosed(1, 10)
                .peek(i -> {
                    System.out.println("第一次peek");
                    System.out.println(i);
                })
                .filter(i -> i > 5)
                .peek(i -> {
                    System.out.println("第二次peek");
                    System.out.println(i);
                })
                .filter(i -> i % 2 == 0)
                .forEach(i -> {
                    System.out.println("最终结果");
                    System.out.println(i);
                });

//        orders.stream()
//                .filter(order -> order.getTotalPrice() > 40)
//                .peek(order -> System.out.println(order.toString()))
//                .map(Order::getCustomerName)
//                .collect(toList());


    }

    /**
     * @description  课后习题  实现一个 MostPopularCollector，来得到 List 中出现次数最多的元素
     */
    @Test
    public void customCollector() //自定义收集器
    {
        //最受欢迎收集器
        assertThat(Stream.of(1, 1, 2, 2, 2, 3, 4, 5, 5).collect(new MostPopularCollector<>()).get(), is(2));
        assertThat(Stream.of('a', 'b', 'c', 'c', 'c', 'd').collect(new MostPopularCollector<>()).get(), is('c'));
        assertThat(Stream.concat(Stream.concat(IntStream.rangeClosed(1, 1000).boxed(), IntStream.rangeClosed(1, 1000).boxed()), Stream.of(2))
                .parallel().collect(new MostPopularCollector<>()).get(), is(2));

    }
}
