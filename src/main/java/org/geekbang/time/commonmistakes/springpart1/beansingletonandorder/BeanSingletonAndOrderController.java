package org.geekbang.time.commonmistakes.springpart1.beansingletonandorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("beansingletonandorder")
public class BeanSingletonAndOrderController {

    @Autowired
    List<SayService> sayServiceList;
    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("test")
    public void test() {
        log.info("====================");
        sayServiceList.forEach(SayService::say);
    }

    /**
     * @description 如果不希望走代理的话还有一种方式是，每次直接从 ApplicationContext 中获取 Bean：
     */
    @GetMapping("test2")
    public void test2() {
        log.info("====================");
        applicationContext.getBeansOfType(SayService.class).values().forEach(SayService::say);
    }
}
