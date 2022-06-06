package org.geekbang.time.commonmistakes.clientdata.trustclientuserid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @description 自动实现参数的组装
 */
@Slf4j
public class LoginRequiredArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * @description  确定解析哪些参数
     * upportsParameter 方法判断当参数上有 @LoginRequired 注解时，再做自定义参数 解析的处理
     */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        //匹配参数上具有@LoginRequired注解的参数
        return methodParameter.hasParameterAnnotation(LoginRequired.class);
    }


    /**
     * @description resolveArgument 方法用来实现解析逻辑本身
     * 尝试从 Session 中获取当 前用户的标识，如果无法获取到的话提示非法调用的错误，如果获取到则返回 userId
     */
    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        //从参数上获得注解
        LoginRequired loginRequired = methodParameter.getParameterAnnotation(LoginRequired.class);
        //根据注解中的Session Key，从Session中查询用户信息
        //key=sessionKey(默认 currentUser)  value=存在session中的userID  因为一旦登陆了  会在session中：session.setAttribute("currentUser", 1L);
        Object object = nativeWebRequest.getAttribute(loginRequired.sessionKey(), NativeWebRequest.SCOPE_SESSION);
        if (object == null) {
            log.error("接口 {} 非法调用！", methodParameter.getMethod().toString());
            throw new RuntimeException("请先登录！");
        }
        return object;
    }
}
