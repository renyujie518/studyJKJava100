package org.geekbang.time.commonmistakes.serialization.jsonignoreproperties;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.geekbang.time.commonmistakes.common.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CommonMistakesApplication {

    public static void main(String[] args) {
        Utils.loadPropertySource(CommonMistakesApplication.class, "jackson.properties");
        SpringApplication.run(CommonMistakesApplication.class, args);
    }

    /**
     * @description 错误做法
     * 因为反序列化的时候，原始数据多了一个 version 属性
     */
//    @Bean
//    public ObjectMapper objectMapper() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true);
//        return objectMapper;
//    }


    /**
     * @description 直接定义 Jackson2ObjectMapperBuilderCustomizer Bean 来启用新特性：
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder.featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
    }
}

