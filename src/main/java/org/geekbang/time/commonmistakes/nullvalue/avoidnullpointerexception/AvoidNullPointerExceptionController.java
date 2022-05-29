package org.geekbang.time.commonmistakes.nullvalue.avoidnullpointerexception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @description 11-1  常见的空指针异常情况
 */
@RestController
@RequestMapping("avoidnullpointerexception")
@Slf4j
public class AvoidNullPointerExceptionController {

    @GetMapping("wrong")
    public int wrong(@RequestParam(value = "test", defaultValue = "1111") String test) {
        return wrongMethod(test.charAt(0) == '1' ? null : new FooService(),
                test.charAt(1) == '1' ? null : 1,
                test.charAt(2) == '1' ? null : "OK",
                test.charAt(3) == '1' ? null : "OK").size();
    }

    @GetMapping("right")
    public int right(@RequestParam(value = "test", defaultValue = "1111") String test) {
        return Optional.ofNullable(rightMethod(test.charAt(0) == '1' ? null : new FooService(),
                test.charAt(1) == '1' ? null : 1,
                test.charAt(2) == '1' ? null : "OK",
                test.charAt(3) == '1' ? null : "OK"))
                .orElse(Collections.emptyList()).size();
    }


    private List<String> wrongMethod(FooService fooService, Integer i, String s, String t) {
        log.info("result {} {} {} {}",
                //对入参 Integer i 进行 +1 操作
                i + 1,
                //对入参 String s 进行比较操作，判断内容是否等于"OK"；
                s.equals("OK"),
                //对入参 String s 和入参 String t 进行比较操作，判断两者是否相等
                s.equals(t),
                //对 new 出来的 ConcurrentHashMap 进行 put 操作，Key 和 Value 都设置为 null。
                new ConcurrentHashMap<String, String>().put(null, null));
        //级联调用 可能空指针的地方有很多，包括 fooService、getBarService() 方法的返回值，以及 bar 方法返回的字符串
        if (fooService.getBarService().bar().equals("OK"))
            log.info("OK");
        return null;
    }

    private List<String> rightMethod(FooService fooService, Integer i, String s, String t) {
        log.info("result {} {} {} {}",
                //默认为0 保证拆箱时不空指针异常
                Optional.ofNullable(i).orElse(0) + 1,
                //字面量放在前面
                "OK".equals(s),
                //可能为 null 的字符串变量的 equals 比 较，可以使用 Objects.equals，它会做判空处理
                Objects.equals(s, t),
                //HashMap 的 Key 和 Value 可以存入 null  替换ConcurrentHashMap
                new HashMap<String, String>().put(null, null));

        /**   if (fooService.getBarService().bar().equals("OK"))
              log.info("OK");
         **/
        //级联调用 采用Optional
        Optional.ofNullable(fooService)
                .map(FooService::getBarService)
                .filter(barService -> "OK".equals(barService.bar()))
                .ifPresent(result -> log.info("OK"));
        return new ArrayList<>();
    }

    class FooService {
        @Getter
        private BarService barService;

    }

    class BarService {
        String bar() {
            return "OK";
        }
    }
}
