package org.geekbang.time.commonmistakes.exception.threadpoolandexception;

import jodd.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/**
 * @description 12-4  线程池异常处理
 */
@RestController
@Slf4j
@RequestMapping("threadpoolandexception")
public class ThreadPoolAndExceptionController {

    /**
     * @description 设置全局的默认未捕获异常处理程序
     */
    static {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> log.error("Thread {} got exception", thread, throwable));
    }

    /**
     * @description 提交 10 个任务到线程池异步处理，第 5 个任务抛出一个 RuntimeException
     * 以 execute 方法提交到线程池的异步任务，最好在任务内部做好异常处理
     */
    @GetMapping("execute")
    public void execute() throws InterruptedException {

        String prefix = "test";
        ExecutorService threadPool = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder()
                .setNameFormat(prefix + "%d")
                //设置自定义的异常处理程序作为保底
                .setUncaughtExceptionHandler((thread, throwable) -> log.error("ThreadPool {} got exception", thread, throwable))
                .get());
        IntStream.rangeClosed(1, 10).forEach(i -> threadPool.execute(() -> {
            if (i == 5) throw new RuntimeException("error");
            log.info("I'm done : {}", i);
        }));

        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);
    }

    /**
     * @description 把 execute 方法改为 submit，线程 还会退出时异常不能被处理程序捕获到
     * 说明线程没退出，异常也没记录被生吞了
     */
    @GetMapping("submit")
    public void submit() throws InterruptedException {

        String prefix = "test";
        ExecutorService threadPool = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat(prefix + "%d").get());
        IntStream.rangeClosed(1, 10).forEach(i -> threadPool.submit(() -> {
            if (i == 5) throw new RuntimeException("error");
            log.info("I'm done : {}", i);
        }));

        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);
    }

    @GetMapping("submitright")
    public void submitRight() throws InterruptedException {

        String prefix = "test";
        ExecutorService threadPool = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat(prefix + "%d").get());

        /** 为解决上述问题
         * 把 submit 返回的 Future 放到了 List 中，随后遍历 List
         * 调用其 get 方法来获得任务运行结果和可能出现的异常，否则异常可能就被生吞了**/
        List<Future> tasks = IntStream.rangeClosed(1, 10).mapToObj(i -> threadPool.submit(() -> {
            if (i == 5) throw new RuntimeException("error");
            log.info("I'm done : {}", i);
        })).collect(Collectors.toList());

        tasks.forEach(task -> {
            try {
                task.get();
            } catch (Exception e) {
                log.error("Got exception", e);
            }
        });
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);
    }
}
