package org.geekbang.time.commonmistakes.threadpool.threadpooloom;

import jodd.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @description 03-1
 * 线程池
 */
@RestController
@RequestMapping("threadpooloom")
@Slf4j
public class ThreadPoolOOMController {

    /**
     * @description 辅助监控
     * 每秒输出一次线程 池的基本内部信息，包括线程数、活跃线程数、完成了多少任务，以及队列中还有多少积压 任务等信息
     */
    private void printStats(ThreadPoolExecutor threadPool) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            log.info("=========================");
            log.info("Pool Size: {}", threadPool.getPoolSize());
            log.info("Active Threads: {}", threadPool.getActiveCount());
            log.info("Number of Tasks Completed: {}", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());

            log.info("=========================");
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * @description 来初始化一个单线程的 FixedThreadPool，
     * 循环 1 亿次向线程池提 交任务，每个任务都会创建一个比较大的字符串然后休眠一小时（相当于每个认为1小时执行完成）
     * 会导致任务队列积压
     */
    @GetMapping("oom1")
    public void oom1() throws InterruptedException {

        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        printStats(threadPool);
        for (int i = 0; i < 100000000; i++) {
            threadPool.execute(() -> {
                String payload = IntStream.rangeClosed(1, 1000000)
                        .mapToObj(__ -> "a")
                        .collect(Collectors.joining("")) + UUID.randomUUID().toString();
                try {
                    TimeUnit.HOURS.sleep(1);
                } catch (InterruptedException e) {
                }
                log.info(payload);
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);
    }

    /**
     * @description 来初始化一个单线程的 newCachedThreadPool，
     * 循环 1 亿次向线程池提 交任务，每个任务都会创建一个比较大的字符串然后休眠一小时（相当于每个认为1小时执行完成）
     * 会导致大量任务创建大量线程，消耗内存空间
     */
    @GetMapping("oom2")
    public void oom2() throws InterruptedException {

        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        printStats(threadPool);
        for (int i = 0; i < 100000000; i++) {
            threadPool.execute(() -> {
                String payload = UUID.randomUUID().toString();
                try {
                    TimeUnit.HOURS.sleep(1);
                } catch (InterruptedException e) {
                }
                log.info(payload);
            });
        }
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);
    }

    /**
     * @description 自定义线程池的正确用法
     * 这个线程池具有 2 个核心线程、5 个最大线程、使用容量为 10 的 ArrayBlockingQueue 阻塞队列作为工作队列，
     * 使用默认的 AbortPolicy 拒绝策略，也 就是任务添加到线程池失败会抛出 RejectedExecutionException。
     * 此外，我们借助了 Jodd 类库的 ThreadFactoryBuilder 方法来构造一个线程工厂，实现线程池线程的自定义 命名。
     */
    @GetMapping("right")
    public int right() throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                2,
                5,
                5,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                new ThreadFactoryBuilder().setNameFormat("demo-threadpool-%d").get(),
                new ThreadPoolExecutor.AbortPolicy());
        //threadPool.allowCoreThreadTimeOut(true);
        printStats(threadPool);
        //，每次间隔 1 秒向线程池提交任务，循环 20 次，每个任务需要 10 秒才能执行完成，
        IntStream.rangeClosed(1, 20).forEach(i -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int id = atomicInteger.incrementAndGet();
            try {
                threadPool.submit(() -> {
                    log.info("{} started", id);
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                    }
                    log.info("{} finished", id);
                });
            } catch (Exception ex) {
                //提交出现异常的话，打印出错信息并为计数器减一
                log.error("error submitting task {}", id, ex);
                atomicInteger.decrementAndGet();
            }
        });

        TimeUnit.SECONDS.sleep(60);
        return atomicInteger.intValue();
    }

    @GetMapping("better")
    public int better() throws InterruptedException {
        //这里开始是激进线程池的实现
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(10) {
            @Override
            public boolean offer(Runnable e) {
                //先返回false，造成队列满的假象，让线程池优先扩容
                return false;
            }
        };

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                2, 5,
                5, TimeUnit.SECONDS,
                queue, new ThreadFactoryBuilder().setNameFormat("demo-threadpool-%d").get(), (r, executor) -> {
            try {
                //等出现拒绝后再加入队列
                //如果希望队列满了阻塞线程而不是抛出异常，那么可以注释掉下面三行代码，修改为executor.getQueue().put(r);
                if (!executor.getQueue().offer(r, 0, TimeUnit.SECONDS)) {
                    throw new RejectedExecutionException("ThreadPool queue full, failed to offer " + r.toString());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        //激进线程池实现结束

        printStats(threadPool);
        //每秒提交一个任务，每个任务耗时10秒执行完成，一共提交20个任务

        //任务编号计数器
        AtomicInteger atomicInteger = new AtomicInteger();

        IntStream.rangeClosed(1, 20).forEach(i -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int id = atomicInteger.incrementAndGet();
            try {
                threadPool.submit(() -> {
                    log.info("{} started", id);
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                    }
                    log.info("{} finished", id);
                });
            } catch (Exception ex) {
                log.error("error submitting task {}", id, ex);
                atomicInteger.decrementAndGet();
            }
        });

        TimeUnit.SECONDS.sleep(60);
        return atomicInteger.intValue();
    }
}
