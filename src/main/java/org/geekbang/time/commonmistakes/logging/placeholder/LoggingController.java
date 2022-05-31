package org.geekbang.time.commonmistakes.logging.placeholder;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
/**
 * @description 13-3 日志占位符问题
 */
@Log4j2
@RequestMapping("logging")
@RestController
public class LoggingController {

    @GetMapping
    public void index() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("debug1");
        //拼接字符串方式记录 slowString；
        log.debug("debug1:" + slowString("debug1"));
        stopWatch.stop();
        stopWatch.start("debug2");
        //使用占位符方式记录 slowString；
        log.debug("debug2:{}", slowString("debug2"));
        stopWatch.stop();
        stopWatch.start("debug3");
        //先判断日志级别是否启用 DEBUG。
        if (log.isDebugEnabled())
            log.debug("debug3:{}", slowString("debug3"));
        stopWatch.stop();
        stopWatch.start("debug4");
        //把 Lombok 的 @Slf4j 注解替换为 @Log4j2 注解，这样就可以提供一个 lambda 表达式作为提供参数数据的方 法：
        log.debug("debug4:{}", () -> slowString("debug4"));
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());

    }

    private String slowString(String s) {
        System.out.println("slowString called via " + s);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
        }
        return "OK";
    }
}
