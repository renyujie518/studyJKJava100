package org.geekbang.time.commonmistakes.productionready.health;

import jodd.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolProvider {
    /**
     * @description 包含一个工 作线程的线程池，类型是 ArrayBlockingQueue，阻塞队列的长度为 10
     */
    private static ThreadPoolExecutor demoThreadPool = new ThreadPoolExecutor(
            1, 1,
            2, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10),
            new ThreadFactoryBuilder().setNameFormat("demo-threadpool-%d").get());


    /**
     * @description IO 操作线程池，核心线程数 10，最大线程数 50
     */
    private static ThreadPoolExecutor ioThreadPool = new ThreadPoolExecutor(
            10, 50,
            2, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadFactoryBuilder().setNameFormat("io-threadpool-%d").get());

    public static ThreadPoolExecutor getDemoThreadPool() {
        return demoThreadPool;
    }

    public static ThreadPoolExecutor getIOThreadPool() {
        return ioThreadPool;
    }
}
