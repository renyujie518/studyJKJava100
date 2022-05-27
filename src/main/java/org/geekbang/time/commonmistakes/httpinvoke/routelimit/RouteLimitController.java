package org.geekbang.time.commonmistakes.httpinvoke.routelimit;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * @description 05-3
 * 并发数的限制导致程 序的处理能力上不去。
 */
@Slf4j
@RequestMapping("routelimit")
@RestController
public class RouteLimitController {

    static CloseableHttpClient httpClient1;
    static CloseableHttpClient httpClient2;

    static {
        //使用默认的 PoolingHttpClientConnectionManager 构造的 CloseableHttpClient
        httpClient1 = HttpClients.custom().setConnectionManager(new PoolingHttpClientConnectionManager()).build();
        //设置 maxPerRoute 为 50、maxTotal 为 100（总数=同一个主机 / 域名的最大并发请求数10 * ）
        httpClient2 = HttpClients.custom().setMaxConnPerRoute(10).setMaxConnTotal(20).build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                httpClient1.close();
            } catch (IOException ex) {
            }
            try {
                httpClient2.close();
            } catch (IOException ex) {
            }
        }));
    }

    private int sendRequest(int count, Supplier<CloseableHttpClient> client) throws InterruptedException {
        //用于计数发送的请求个数
        AtomicInteger atomicInteger = new AtomicInteger();
        //使用一个 没有线程上限的 newCachedThreadPool 作为爬取任务的线程池
        ExecutorService threadPool = Executors.newCachedThreadPool();
        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, count).forEach(i -> {
            threadPool.execute(() -> {
                //使用HttpClient从server接口查询数据的任务提交到线程池并行处理
                try (CloseableHttpResponse response = client.get().execute(new HttpGet("http://127.0.0.1:45678/routelimit/server"))) {
                    atomicInteger.addAndGet(Integer.parseInt(EntityUtils.toString(response.getEntity())));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });
        //等到count个任务全部执行完毕
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);
        log.info("发送 {} 次请求，耗时 {} ms", atomicInteger.get(), System.currentTimeMillis() - begin);
        return atomicInteger.get();
    }

    @GetMapping("wrong")
    public int wrong(@RequestParam(value = "count", defaultValue = "10") int count) throws InterruptedException {
        return sendRequest(count, () -> httpClient1);
    }

    @GetMapping("right")
    public int right(@RequestParam(value = "count", defaultValue = "10") int count) throws InterruptedException {
        return sendRequest(count, () -> httpClient2);
    }

    @GetMapping("server")
    public int server() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        return 1;
    }
}
