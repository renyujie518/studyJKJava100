package org.geekbang.time.commonmistakes.productionready.info;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
/**
 * @description 24-2
 * 通过 Actuator 的 InfoContributor 功能 来展现线程池的信息（主要就是map的方式）
 */
@Component
public class ThreadPoolInfoContributor implements InfoContributor {
    private static Map threadPoolInfo(ThreadPoolExecutor threadPool) {
        Map<String, Object> info = new HashMap<>();
        info.put("poolSize", threadPool.getPoolSize());
        info.put("corePoolSize", threadPool.getCorePoolSize());
        info.put("largestPoolSize", threadPool.getLargestPoolSize());
        info.put("maximumPoolSize", threadPool.getMaximumPoolSize());
        info.put("completedTaskCount", threadPool.getCompletedTaskCount());
        return info;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("demoThreadPool", threadPoolInfo(ThreadPoolProvider.getDemoThreadPool()));
        builder.withDetail("ioThreadPool", threadPoolInfo(ThreadPoolProvider.getIOThreadPool()));
    }
}
