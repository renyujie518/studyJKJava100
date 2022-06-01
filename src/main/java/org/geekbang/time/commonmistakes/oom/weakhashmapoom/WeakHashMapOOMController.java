package org.geekbang.time.commonmistakes.oom.weakhashmapoom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
/**
 * @description 17-2 WeakHashMap的OOM问题  相互引用导致的数据不能释放
 */
@RestController
@RequestMapping("weakhashmapoom")
@Slf4j
public class WeakHashMapOOMController {
    private Map<User, UserProfile> cache = new WeakHashMap<>();
    private Map<User, WeakReference<UserProfile>> cache2 = new WeakHashMap<>();
    private Map<User, UserProfile> cache3 = new ConcurrentReferenceHashMap<>();

    /**
     * @description WeakHashMap 的 Key 是 User 对象，而其 Value 是 UserProfile 对象，持有了 User 的引用
     */
    @GetMapping("wrong")
    public void wrong() {
        String userName = "zhuye";
        //然后使用 ScheduledThreadPoolExecutor 发起一 个定时任务，每隔 1 秒输出缓存中的 Entry 个数
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> log.info("cache size:{}", cache.size()), 1, 1, TimeUnit.SECONDS);
        LongStream.rangeClosed(1, 2000000).forEach(i -> {
            User user = new User(userName + i);
            cache.put(user, new UserProfile(user, "location" + i));
        });
    }

    /**
     * @description 使用弱引用来包装UserProfile
     */
    @GetMapping("right")
    public void right() {
        String userName = "zhuye";
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> log.info("cache size:{}", cache2.size()), 1, 1, TimeUnit.SECONDS);
        LongStream.rangeClosed(1, 2000000).forEach(i -> {
            User user = new User(userName + i);
            cache2.put(user, new WeakReference(new UserProfile(user, "location" + i)));
        });
    }

    /**
     * @description 让 Value 也就是 UserProfile 不再引用 Key，而是重新 new 出 一个新的 User 对象赋值给 UserProfile
     */
    @GetMapping("right2")
    public void right2() {
        String userName = "zhuye";
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> log.info("cache size:{}", cache.size()), 1, 1, TimeUnit.SECONDS);
        LongStream.rangeClosed(1, 2000000).forEach(i -> {
            User user = new User(userName + i);
            cache.put(user, new UserProfile(new User(user.getName()), "location" + i));
        });
    }

    /**
     * @description ConcurrentReferenceHashMap类可以使用弱引用、软引用做缓 存，
     * Key 和 Value 同时被软引用或弱引用包装，也能解决相互引用导致的数据不能释放问 题。
     * 还线程安全
     */
    @GetMapping("right3")
    public void right3() {
        String userName = "zhuye";
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> log.info("cache size:{}", cache3.size()), 1, 1, TimeUnit.SECONDS);
        LongStream.rangeClosed(1, 20000000).forEach(i -> {
            User user = new User(userName + i);
            cache3.put(user, new UserProfile(user, "location" + i));
        });
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class User {
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class UserProfile {
        private User user;
        private String location;
    }
}
