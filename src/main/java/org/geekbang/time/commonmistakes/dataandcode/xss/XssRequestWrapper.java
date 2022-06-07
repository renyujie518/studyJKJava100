package org.geekbang.time.commonmistakes.dataandcode.xss;

import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Arrays;
/**
 * @description 实现了HttpServletRequestWrapper
 * 实现所有请求参数的 HTML 转义
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        //获取多个参数值的时候对所有参数值应用clean方法逐一清洁
        return Arrays.stream(super.getParameterValues(parameter)).map(this::clean).toArray(String[]::new);
    }

    //清洁请求头
    @Override
    public String getHeader(String name) {
        return clean(super.getHeader(name));
    }

    //清洁单一参数
    @Override
    public String getParameter(String parameter) {
        return clean(super.getParameter(parameter));
    }

    /**
     * @description 清洁的方法实际上也是对参数进行HTML转义
     */
    private String clean(String value) {
        return StringUtils.isEmpty(value) ? "" : HtmlUtils.htmlEscape(value);
    }
}
