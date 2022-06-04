package org.geekbang.time.commonmistakes.apidesign.headerapiversion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * @description 22-课后作业 基于请求头方式的版本控制
 */
@Slf4j
@RequestMapping("apiversion")
@RestController
@APIVersion("v1")
public class APIVersoinController {

    @GetMapping(value = "/api/user")
    public int version1() {
        return 1;
    }

    @GetMapping(value = "/api/user")
    @APIVersion("v2")
    public int version2() {
        return 2;
    }
}
