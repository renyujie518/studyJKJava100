package org.geekbang.time.commonmistakes.cachedesign.cacheinvalid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
/**
 * @description 23-1 缓存雪崩问题  确保大量 Key 不在同一时间被动过期
 */
@Slf4j
@RequestMapping("cacheinvalid")
@RestController
public class CacheInvalidController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private AtomicInteger atomicInteger = new AtomicInteger();

    //@PostConstruct
    public void wrongInit() {
        IntStream.rangeClosed(1, 1000).forEach(i -> stringRedisTemplate.opsForValue().set("city" + i, getCityFromDb(i), 30, TimeUnit.SECONDS));
        log.info("Cache init finished");
        //每秒一次，输出数据库访问的QPS(用累加器模拟的)同时归0
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            log.info("DB QPS : {}", atomicInteger.getAndSet(0));
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * @description 设置缓存的过期时间是 30 秒 +10 秒以内的随机延迟（扰动值）
     */
    //@PostConstruct
    public void rightInit1() {
        IntStream.rangeClosed(1, 1000).forEach(i -> stringRedisTemplate.opsForValue().set("city" + i, getCityFromDb(i), 30 + ThreadLocalRandom.current().nextInt(10), TimeUnit.SECONDS));
        log.info("Cache init finished");
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            log.info("DB QPS : {}", atomicInteger.getAndSet(0));
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * @description 初始化缓存数据的时候设置缓存永不过期，
     * 然后启动一个后台 线程 30 秒一次定时把所有数据更新到缓存，
     * 而且通过适当的休眠，控制从数据库更新数据 的频率，降低数据库压力
     */
    @PostConstruct
    public void rightInit2() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //每隔30秒全量更新一次缓存
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            IntStream.rangeClosed(1, 1000).forEach(i -> {
                String data = getCityFromDb(i);
                //模拟更新缓存需要一定的时间
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (InterruptedException e) {
                }
                if (!StringUtils.isEmpty(data)) {
                    //缓存永不过期，被动更新
                    stringRedisTemplate.opsForValue().set("city" + i, data);
                }
            });
            log.info("Cache update finished");
            //启动程序的时候需要等待首次更新缓存完成
            countDownLatch.countDown();
        }, 0, 30, TimeUnit.SECONDS);

        //每秒一次，输出数据库访问的QPS(用累加器模拟的)同时归0
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            log.info("DB QPS : {}", atomicInteger.getAndSet(0));
        }, 0, 1, TimeUnit.SECONDS);

        countDownLatch.await();
    }

    /**
     * @description 随机查询某城市信息的接口，用于观察一下数据库的 QPS
     */
    @GetMapping("city")
    public String city() {
        //随机从redis中查询一个城市
        int id = ThreadLocalRandom.current().nextInt(1000) + 1;
        String key = "city" + id;
        String data = stringRedisTemplate.opsForValue().get(key);
        if (data == null) {
            //回源到数据库查询
            data = getCityFromDb(id);
            if (!StringUtils.isEmpty(data))
                //再重新缓存到redis中，同时设置30秒过期
                stringRedisTemplate.opsForValue().set(key, data, 30, TimeUnit.SECONDS);
        }
        return data;
    }


    private String getCityFromDb(int cityId) {
        atomicInteger.incrementAndGet();
        return "citydata" + System.currentTimeMillis();
    }
}
