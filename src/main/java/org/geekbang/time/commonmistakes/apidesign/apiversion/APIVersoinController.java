package org.geekbang.time.commonmistakes.apidesign.apiversion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
/**
 * @description 22-2 接口变迁的版本控制策略
 */
@Slf4j
@RequestMapping("apiversion")
@RestController
@APIVersion("v1")
public class APIVersoinController {

    @GetMapping("/api/item/v1")
    public void wrong1() {

    }

    @GetMapping("/api/v1/shop")
    public void wrong2() {

    }

    @GetMapping("/v1/api/merchant")
    public void wrong3() {

    }

    //通过URL Path实现版本控制
    @GetMapping("/v1/api/user")
    public int right1() {
        return 1;
    }

    //通过QueryString中的version参数实现版本控制
    @GetMapping(value = "/api/user", params = "version=2")
    public int right2(@RequestParam("version") int version) {
        return 2;
    }
    //通过请求头中的X-API-VERSION参数实现版本控制
    @GetMapping(value = "/api/user", headers = "X-API-VERSION=3")
    public int right3(@RequestHeader("X-API-VERSION") int version) {
        return 3;
    }

    @GetMapping(value = "/api/user")
    @APIVersion("v4")
    public int right4() {
        return 4;
    }
}
