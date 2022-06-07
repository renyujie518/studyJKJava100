package org.geekbang.time.commonmistakes.dataandcode.xss;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @description 自定义过滤器
 * 定义一个 servlet Filter，通过 HttpServletRequestWrapper 实现 servlet 层面的统一参数替换
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XssFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }
}
