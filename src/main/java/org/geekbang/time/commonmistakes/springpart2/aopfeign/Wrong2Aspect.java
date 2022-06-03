package org.geekbang.time.commonmistakes.springpart2.aopfeign;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
@Slf4j
//@Component
public class Wrong2Aspect {

    /**
     * @description 通过 @FeignClient 注 解来切
     * 会导致切入的是 ClientWithUrl 接口的 API 方法，并不是 client.Feign 接口的 execute 方法
     */
    @Before("@within(org.springframework.cloud.openfeign.FeignClient)")
    public void before(JoinPoint pjp) {
        log.info("@within(org.springframework.cloud.openfeign.FeignClient) pjp {}, args:{}", pjp, pjp.getArgs());
    }
}
