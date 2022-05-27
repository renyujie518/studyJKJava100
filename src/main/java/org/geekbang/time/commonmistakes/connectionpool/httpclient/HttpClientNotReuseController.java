package org.geekbang.time.commonmistakes.connectionpool.httpclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
/**
 * @description 04-2  连接池释放问题
 */
@RestController
@RequestMapping("httpclientnotreuse")
@Slf4j
public class HttpClientNotReuseController {


    /**
     * @description 把 CloseableHttpClient 声明为 static，只创建一次 实现对client的复用
     * 在 JVM 关闭之前通过 addShutdownHook 钩子关闭连接池
     * 在使用的时候直接使用 CloseableHttpClient 即可，无需每次都创建。
     */
    private static CloseableHttpClient httpClient = null;

    //最大连接数设置为 1。所以，复用连接池方式复用的始终应该是同一个连接
    static {
        httpClient = HttpClients.custom()
                .setMaxConnPerRoute(1)
                .setMaxConnTotal(1)
                .evictIdleConnections(60, TimeUnit.SECONDS)
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                httpClient.close();
            } catch (IOException ignored) {
            }
        }));
    }

    /**
     * @description PoolingHttpClientConnectionManager 连接池
     * 启用空闲连接驱逐策略，最大空闲时间 为 60 秒
     */
    @GetMapping("wrong1")
    public String wrong1() {
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .evictIdleConnections(60, TimeUnit.SECONDS).build();
        try (CloseableHttpResponse response = client.execute(new HttpGet("http://127.0.0.1:45678/httpclientnotreuse/test"))) {
            return EntityUtils.toString(response.getEntity());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @GetMapping("wrong2")
    public String wrong2() {
        try (CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .evictIdleConnections(60, TimeUnit.SECONDS).build();
             CloseableHttpResponse response = client.execute(new HttpGet("http://127.0.0.1:45678/httpclientnotreuse/test"))) {
            return EntityUtils.toString(response.getEntity());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @GetMapping("right")
    public String right() {
        try (CloseableHttpResponse response = httpClient.execute(new HttpGet("http://127.0.0.1:45678/httpclientnotreuse/test"))) {
            return EntityUtils.toString(response.getEntity());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @GetMapping("/test")
    public String test() {
        return "OK";
    }
}
