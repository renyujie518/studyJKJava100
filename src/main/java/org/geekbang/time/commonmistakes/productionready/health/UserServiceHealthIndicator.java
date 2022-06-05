package org.geekbang.time.commonmistakes.productionready.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @description 实现 HealthIndicator 接口
 * 判断user接口是否正确响应和程序整体的健康状态
 */
@Component
@Slf4j
public class UserServiceHealthIndicator implements HealthIndicator {
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Health health() {
        long begin = System.currentTimeMillis();
        long userId = 1L;
        User user = null;
        try {
            //访问远程的user接口
            user = restTemplate.getForObject("http://localhost:45678/user?userId=" + userId, User.class);
            if (user != null && user.getUserId() == userId) {
                //结果正确，返回UP状态，补充提供耗时和用户信息
                return Health.up()
                        .withDetail("user", user)
                        .withDetail("took", System.currentTimeMillis() - begin)
                        .build();
            } else {
                //结果不正确，返回DOWN状态，补充提供耗时
                return Health.down().withDetail("took", System.currentTimeMillis() - begin).build();
            }
        } catch (Exception ex) {
            //出现异常，先记录异常，然后返回DOWN状态，补充提供异常信息和耗时
            log.warn("health check failed!", ex);
            return Health.down(ex).withDetail("took", System.currentTimeMillis() - begin).build();
        }
    }
}
