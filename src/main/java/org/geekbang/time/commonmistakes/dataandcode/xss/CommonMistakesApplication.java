package org.geekbang.time.commonmistakes.dataandcode.xss;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CommonMistakesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonMistakesApplication.class, args);
    }

    /**
     * @description 注册自定义的Jackson序列器
     */
    @Bean
    public Module xssModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new XssJsonDeserializer());
        module.addSerializer(String.class, new XssJsonSerializer());
        return module;
    }
}

