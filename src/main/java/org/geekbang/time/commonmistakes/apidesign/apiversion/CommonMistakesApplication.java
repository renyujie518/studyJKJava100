package org.geekbang.time.commonmistakes.apidesign.apiversion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class CommonMistakesApplication implements WebMvcRegistrations {

    public static void main(String[] args) {
        SpringApplication.run(CommonMistakesApplication.class, args);
    }

    /**
     * @description 要通过实现 WebMvcRegistrations 接口，来生效自定 义的 APIVersionHandlerMapping：
     */
    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new APIVersionHandlerMapping();
    }
}

