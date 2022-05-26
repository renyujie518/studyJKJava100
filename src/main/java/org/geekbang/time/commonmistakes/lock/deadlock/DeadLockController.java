package org.geekbang.time.commonmistakes.lock.deadlock;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @description 02-3
 * 死锁问题
 */
@RestController
@RequestMapping("deadlock")
@Slf4j
public class DeadLockController {

    private ConcurrentHashMap<String, Item> items = new ConcurrentHashMap<>();

    public DeadLockController() {
        IntStream.range(0, 10).forEach(i -> items.put("item" + i, new Item("item" + i)));
    }

    private boolean createOrder(List<Item> order) {
        //存放所有获得的锁
        List<ReentrantLock> locks = new ArrayList<>();

        //遍历购物车中的商品依次尝试 获得商品的锁，最长等待 10 秒
        for (Item item : order) {
            try {
                if (item.lock.tryLock(10, TimeUnit.SECONDS)) {
                    locks.add(item.lock);
                } else {
                    //无法获得锁的情况则 解锁之前获得的所有锁，返回 false 下单失败
                    locks.forEach(ReentrantLock::unlock);
                    return false;
                }
            } catch (InterruptedException e) {
            }
        }
        //获得全部锁之后再扣减库存
        try {
            order.forEach(item -> item.remaining--);
        } finally {
            locks.forEach(ReentrantLock::unlock);
        }
        return true;
    }

    //模拟在购物车进行商品选购，每次从商品清单（items 字段）中随机选购 三个商品
    private List<Item> createCart() {
        return IntStream.rangeClosed(1, 3)
                .mapToObj(i -> "item" + ThreadLocalRandom.current().nextInt(items.size()))
                .map(name -> items.get(name))
                .collect(Collectors.toList());
    }

    /**
     * @description 模拟在多线程情况下进行 100 次创建购物车和下单操 作
     */
    @GetMapping("wrong")
    public long wrong() {
        long begin = System.currentTimeMillis();
        long success = IntStream.rangeClosed(1, 100).parallel()
                .mapToObj(i -> {
                    List<Item> cart = createCart();
                    return createOrder(cart);
                })
                .filter(result -> result)
                .count();
        log.info("success:{} totalRemaining:{} took:{}ms items:{}",
                success,
                items.entrySet().stream().map(item -> item.getValue().remaining).reduce(0, Integer::sum),
                System.currentTimeMillis() - begin, items);
        return success;
    }

    @GetMapping("right")
    public long right() {
        long begin = System.currentTimeMillis();
        //只保留成功的次数
        long success = IntStream.rangeClosed(1, 100).parallel()
                .mapToObj(i -> {
                    //多了一步骤   对 createCart 获得的购物车按照商品名进行排序
                    List<Item> cart = createCart().stream()
                            .sorted(Comparator.comparing(Item::getName))
                            .collect(Collectors.toList());
                    return createOrder(cart);
                })
                .filter(result -> result)
                .count();
        //成功的下单次数、总剩余的商品个数、100 次下单耗时，以及下单完成后的商品库存明细
        log.info("success:{} totalRemaining:{} took:{}ms items:{}",
                success,
                items.entrySet().stream().map(item -> item.getValue().remaining).reduce(0, Integer::sum),
                System.currentTimeMillis() - begin,
                items);
        return success;
    }

    @Data
    @RequiredArgsConstructor
    static class Item {
        final String name;
        int remaining = 1000;
        //下面这个注解表示ToString不包含这个字段
        @ToString.Exclude
        ReentrantLock lock = new ReentrantLock();
    }
}
