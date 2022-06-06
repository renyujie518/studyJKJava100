package org.geekbang.time.commonmistakes.clientdata.trustclientuserid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
/**
 * @description 27-4 用户标识不能从客户端获取
 */
@Slf4j
@RequestMapping("trustclientuserid")
@RestController
public class TrustClientUserIdController {

    @GetMapping("wrong")
    public String wrong(@RequestParam("userId") Long userId) {
        return "当前用户Id：" + userId;
    }

    /**
     * @description LoginRequiredArgumentResolver会将修饰了@LoginRequired的userId变量自动从session赋值
     * 一旦是已登录的userId（即在本例中是1）那么相当于此时会输出  当前用户Id：1
     * 经过这样的实现，登录后所有需要登录的方法都可以一键通过加 @LoginRequired 注解来拿到用户标识，方便且安全
     */
    @GetMapping("right")
    public String right(@LoginRequired Long userId) {
        return "当前用户Id：" + userId;
    }

    /**
     * @description 登录后在 Session 中设置了当前用户的标识  返回userid
     */
    @GetMapping("login")
    public long login(@RequestParam("username") String username, @RequestParam("password") String password, HttpSession session) {
        if (username.equals("admin") && password.equals("admin")) {
            session.setAttribute("currentUser", 1L);
            return 1L;
        }
        return 0L;
    }
}
