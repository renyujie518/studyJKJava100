package org.geekbang.time.commonmistakes.springpart2.aopfeign;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
/**
 * @description 使用 within 指示器匹配 feign.Client 接口的实现进行 AOP 切入。
 */
@Aspect
@Slf4j
@Component
public class WrongAspect {
    @Before("within(feign.Client+)")  //代表AOP切入feign.Client的实现 即实现了feign.Client的接口下的子类
    public void before(JoinPoint pjp) {
        log.info("within(feign.Client+) pjp {}, args:{}", pjp, pjp.getArgs());
    }
}
