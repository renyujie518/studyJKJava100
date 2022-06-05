package org.geekbang.time.commonmistakes.productionready.health;

import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.NamedContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * @description 24-1 聚合两个 ThreadPoolHealthIndicator 的 实例
 * 分别对应 ThreadPoolProvider 中定义的两个线程池(demo和IO)
 */
@Component
public class ThreadPoolsHealthContributor implements CompositeHealthContributor {

    //保存所有的子HealthContributor
    private Map<String, HealthContributor> contributors = new HashMap<>();

    //初始化  对应ThreadPoolProvider中定义的两个线程池
    ThreadPoolsHealthContributor() {
        this.contributors.put("demoThreadPool", new ThreadPoolHealthIndicator(ThreadPoolProvider.getDemoThreadPool()));
        this.contributors.put("ioThreadPool", new ThreadPoolHealthIndicator(ThreadPoolProvider.getIOThreadPool()));
    }

    @Override
    public HealthContributor getContributor(String name) {
        return contributors.get(name);
    }

    /**
     * @description 返回NamedContributor的迭代器
     */
    @Override
    public Iterator<NamedContributor<HealthContributor>> iterator() {
        return contributors.entrySet().stream()
                .map((entry) -> NamedContributor.of(entry.getKey(), entry.getValue())).iterator();
    }
}
