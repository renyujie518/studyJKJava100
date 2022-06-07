package org.geekbang.time.commonmistakes.java8;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
/**
 * @description 并行流
 */
@Slf4j
public class ParallelTest {

    @Test
    public void parallel() {
        IntStream.rangeClosed(1, 100).parallel().forEach(i -> {
            System.out.println(LocalDateTime.now() + " : " + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        });
    }

    @Test
    public void allMethods() throws InterruptedException, ExecutionException {
        int taskCount = 10000;
        int threadCount = 20;
        StopWatch stopWatch = new StopWatch();

        stopWatch.start("thread");
        Assert.assertEquals(taskCount, thread(taskCount, threadCount));
        stopWatch.stop();

        stopWatch.start("threadpool");
        Assert.assertEquals(taskCount, threadpool(taskCount, threadCount));
        stopWatch.stop();

        //试试把这段放到forkjoin下面？
        stopWatch.start("stream");
        Assert.assertEquals(taskCount, stream(taskCount, threadCount));
        stopWatch.stop();

        stopWatch.start("forkjoin");
        Assert.assertEquals(taskCount, forkjoin(taskCount, threadCount));
        stopWatch.stop();

        stopWatch.start("completableFuture");
        Assert.assertEquals(taskCount, completableFuture(taskCount, threadCount));
        stopWatch.stop();

        log.info(stopWatch.prettyPrint());
    }

    private void increment(AtomicInteger atomicInteger) {
        atomicInteger.incrementAndGet();
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * @description 直接使用线程。直接把任务按照线程数均匀分割，分配到不同的线程执行
     */
    private int thread(int taskCount, int threadCount) throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        //使用CountDownLatch来等待所有线程执行完成
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        IntStream.rangeClosed(1, threadCount).mapToObj(i -> new Thread(() -> {
            IntStream.rangeClosed(1, taskCount / threadCount).forEach(j -> increment(atomicInteger));
            //每一个线程处理完成自己那部分数据之后，countDown一次
            countDownLatch.countDown();
        })).forEach(Thread::start);
        //等到所有线程执行完成
        countDownLatch.await();
        return atomicInteger.get();
    }

    /**
     * @description 使用 Executors.newFixedThreadPool 来获得固定线程数的线程池，
     * 使用 execute 提交所有任务到线程池执行，最后关闭线程池等待所有任务执行完成
     */
    private int threadpool(int taskCount, int threadCount) throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        IntStream.rangeClosed(1, taskCount).forEach(i -> executorService.execute(() -> increment(atomicInteger)));
        //提交关闭线程池申请，等待之前所有任务执行完成
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
        return atomicInteger.get();
    }

    /**
     * @description 使用 ForkJoinPool 而不是普通线程池执行任务
     * ForkJoinPool 和传统的 ThreadPoolExecutor 区别在于，前者对于 n 并行度有 n 个独立 队列，后者是共享队列。
     * 如果有大量执行耗时比较短的任务，ThreadPoolExecutor 的单队 列就可能会成为瓶颈。这时，使用 ForkJoinPool 性能会更好
     */
    private int forkjoin(int taskCount, int threadCount) throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger)));
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
        return atomicInteger.get();
    }

    /**
     * @description 直接使用并行流
     * 并行流内部使用公共的 ForkJoinPool，也就是 ForkJoinPool.commonPool()。
     */
    private int stream(int taskCount, int threadCount) {
        //设置公共ForkJoinPool的并行度
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(threadCount));
        AtomicInteger atomicInteger = new AtomicInteger();
        IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger));
        return atomicInteger.get();
    }

    /**
     * @description 使用 CompletableFuture 来实现。
     * CompletableFuture.runAsync 方法可 以指定一个线程池，一般会在使用 CompletableFuture 的时候用到
     */
    private int completableFuture(int taskCount, int threadCount) throws InterruptedException, ExecutionException {
        AtomicInteger atomicInteger = new AtomicInteger();
        //自定义一个并行度=threadCount的ForkJoinPool
        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        CompletableFuture.runAsync(() -> IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger)), forkJoinPool).get();
        return atomicInteger.get();
    }
}
