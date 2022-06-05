package org.geekbang.time.commonmistakes.productionready.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
/**
 * @description 定义一个 CompositeHealthContributor 来聚合多个 HealthContributor
 * 实现一组线程池的监 控。
 */
public class ThreadPoolHealthIndicator implements HealthIndicator {
    private ThreadPoolExecutor threadPool;

    public ThreadPoolHealthIndicator(ThreadPoolExecutor threadPool) {
        this.threadPool = threadPool;
    }

    /**
     * @description 通过判断队列剩余容量来确定这个线程池的健康状 态
     */
    @Override
    public Health health() {
        Map<String, Integer> detail = new HashMap<>();
        //队列当前元素个数
        detail.put("queue_size", threadPool.getQueue().size());
        //队列剩余容量
        detail.put("queue_remaining", threadPool.getQueue().remainingCapacity());
        if (threadPool.getQueue().remainingCapacity() > 0) {
            //有剩余量则返回 UP
            return Health.up().withDetails(detail).build();
        } else {
            return Health.down().withDetails(detail).build();
        }
    }
}
