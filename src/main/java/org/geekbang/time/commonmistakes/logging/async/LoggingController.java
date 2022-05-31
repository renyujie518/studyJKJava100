package org.geekbang.time.commonmistakes.logging.async;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/**
 * @description 13-2  异步日志问题
 */
@Slf4j
@RequestMapping("logging")
@RestController
public class LoggingController {

    @GetMapping("log")
    public void log() {
        log.debug("debug");
        log.info("info");
        log.warn("warn");
        log.error("error");
    }

    /**
     * @description 默认产生1000条异步日志
     */
    @GetMapping("manylog")
    public void manylog(@RequestParam(name = "count", defaultValue = "1000") int count) {
        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, count).forEach(i -> log.info("log-{}", i));
        System.out.println("took " + (System.currentTimeMillis() - begin) + " ms");
    }

    /**
     * @description 实现了记录指定次数的大日志，每条日志包含 1MB 字节的模拟数据，
     * 最 后记录一条以 time 为标记的方法执行耗时日志
     */
    @GetMapping("performance")
    public void performance(@RequestParam(name = "count", defaultValue = "1000") int count) {
        long begin = System.currentTimeMillis();
        String payload = IntStream.rangeClosed(1, 1000000)
                .mapToObj(__ -> "a")
                .collect(Collectors.joining("")) + UUID.randomUUID().toString();
        IntStream.rangeClosed(1, count).forEach(i -> log.info("{} {}", i, payload));
        Marker timeMarker = MarkerFactory.getMarker("time");
        log.info(timeMarker, "took {} ms", System.currentTimeMillis() - begin);
    }
}
