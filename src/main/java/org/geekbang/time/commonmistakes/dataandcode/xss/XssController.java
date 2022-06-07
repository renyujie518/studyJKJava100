package org.geekbang.time.commonmistakes.dataandcode.xss;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description 29-3  xss攻击问题
 */
@RequestMapping("xss")
@Slf4j
@Controller
public class XssController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String index(ModelMap modelMap) {
        User user = userRepository.findById(1L).orElse(new User());
        //modelMap.addAttribute("username", HtmlUtils.htmlEscape(user.getName()));
        //给View提供Model
        modelMap.addAttribute("username", user.getName());
        return "xss";
    }

    @PostMapping
    public String save(@RequestParam("username") String username, HttpServletRequest request) {
        User user = new User();
        user.setId(1L);
        //注意这里是直接从 HTTP 请求获取数据  而不是不通过 @RequestParam 来获 取数据
        user.setName(request.getParameter("username"));
        userRepository.save(user);
        //保存完成后重定向到首页
        return "redirect:/xss/";
    }

    /**
     * @description 通过 @RequestBody 注解提交的 JSON 数据
     */
    @PutMapping
    public void put(@RequestBody User user) {
        userRepository.save(user);
    }

    @GetMapping("user")
    @ResponseBody
    public User query() {
        return userRepository.findById(1L).orElse(new User());
    }

    @GetMapping("readCookie")
    @ResponseBody
    public String readCookie(@CookieValue("test") String cookieValue) {
        return cookieValue;
    }

    @GetMapping("writeCookie")
    @ResponseBody
    public void writeCookie(@RequestParam("httpOnly") boolean httpOnly, HttpServletResponse response) {
        Cookie cookie = new Cookie("test", "zhuye");
        //开启 HttpOnly 属性   根据httpOnly入参决定是否开启HttpOnly属性
        cookie.setHttpOnly(httpOnly);
        response.addCookie(cookie);
    }
}
