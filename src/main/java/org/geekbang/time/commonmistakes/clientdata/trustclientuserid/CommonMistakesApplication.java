package org.geekbang.time.commonmistakes.clientdata.trustclientuserid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@SpringBootApplication
public class CommonMistakesApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(CommonMistakesApplication.class, args);
    }

    /**
     * @description 实现 WebMvcConfigurer 接口的 addArgumentResolvers 方法，
     * 来增加这 个自定义的处理器 LoginRequiredArgumentResolver
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginRequiredArgumentResolver());
    }
}

