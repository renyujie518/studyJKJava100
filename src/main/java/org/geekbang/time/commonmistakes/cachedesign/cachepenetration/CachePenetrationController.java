package org.geekbang.time.commonmistakes.cachedesign.cachepenetration;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
/**
 * @description 23-3  缓存穿透问题
 */
@Slf4j
@RequestMapping("cachepenetration")
@RestController
public class CachePenetrationController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private AtomicInteger atomicInteger = new AtomicInteger();
    //Google 的 Guava 工具包提供的 BloomFilter
    private BloomFilter<Integer> bloomFilter;

    @PostConstruct
    public void init() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            log.info("DB QPS : {}", atomicInteger.getAndSet(0));
        }, 0, 1, TimeUnit.SECONDS);

        /** 初始化的时候也初始一个一个具有所有有效用户 ID （ID介于0（不含）和10000（包含）之间）的、10000 个元素的 BloomFilter
         * 期望误判率1% **/
        bloomFilter = BloomFilter.create(Funnels.integerFunnel(), 10000, 0.01);
        IntStream.rangeClosed(1, 10000).forEach(bloomFilter::put);
    }

    @GetMapping("wrong")
    public String wrong(@RequestParam("id") int id) {
        String key = "user" + id;
        String data = stringRedisTemplate.opsForValue().get(key);
        //无法区分是无效用户还是缓存失效 如果从数据库查询 ID 不在这个区间的用户，会得到空字符串，
        // 所以缓存中缓存的也是空字符 串。从缓存中查出了空字符串，认为是缓存中没有数据回源查询，其实相当于每次都回源：
        if (StringUtils.isEmpty(data)) {
            data = getCityFromDb(id);
            stringRedisTemplate.opsForValue().set(key, data, 30, TimeUnit.SECONDS);
        }
        return data;
    }

    /**
     * @description 对于不存在的数据，同样设置一个特殊的 Value 到缓存中
     * 比如当数据库中查出 的用户信息为空的时候，设置 NODATA 这样具有特殊含义的字符串到缓存中。
     * 这样下次请 求缓存的时候还是可以命中缓存，即直接从缓存返回结果，不查询数据库
     * 这种方式可能会把大量无效的数据加入缓存中，如果担心大量无效数据占满缓存
     */
    @GetMapping("right")
    public String right(@RequestParam("id") int id) {
        String key = "user" + id;
        String data = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(data)) {
            data = getCityFromDb(id);
            //校验从数据库返回的数据是否有效
            if (!StringUtils.isEmpty(data)) {
                stringRedisTemplate.opsForValue().set(key, data, 30, TimeUnit.SECONDS);
            } else {
                //如果数据库中查出的本来就是null，直接在缓存中设置一个NODATA，这样下次查询时即使是无效用户还是可以命中
                stringRedisTemplate.opsForValue().set(key, "NODATA", 30, TimeUnit.SECONDS);
            }

        }
        return data;
    }

    /**
     * @description 用布隆过滤器做前置过滤。
     * 把所有可能的值保存在布隆过滤器中，从缓存读取数据前先过滤一次
     *
     */
    @GetMapping("right2")
    public String right2(@RequestParam("id") int id) {
        String data = "";
        //通过布隆过滤器先判断   来检测用户 ID 是否可能存在；
        if (bloomFilter.mightContain(id)) {
            String key = "user" + id;
            //先走走缓存查询
            data = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.isEmpty(data)) {
                //没有再回源数据库，同时更新缓存
                data = getCityFromDb(id);
                stringRedisTemplate.opsForValue().set(key, data, 30, TimeUnit.SECONDS);
            }
        }
        return data;
    }

    private String getCityFromDb(int id) {
        atomicInteger.incrementAndGet();
        //注意，只有ID介于0（不含）和10000（包含）之间的用户才是有效用户，可以查询到用户信息
        if (id > 0 && id <= 10000) return "userdata";
        return "";
    }
}
