package org.geekbang.time.commonmistakes.apidesign.apiversion;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * @description 使用框架来明确 API 版本的指定策略，不仅实现了标准化，更实现了强制的 API 版本控 制。
 */
public class APIVersionHandlerMapping extends RequestMappingHandlerMapping {


    @Override
    protected boolean isHandler(Class<?> beanType) {
        return AnnotatedElementUtils.hasAnnotation(beanType, Controller.class);
    }

    /**
     * @description  从 @APIVersion 自定义注解中读取版本信息，
     * 拼接上原有的、不带版本号的 URL Pattern，构成新的 RequestMappingInfo（这个是各个团队约定好的格式）
     * 来通过注解的方式为接口增加基于 URL 的版本号：
     */
    @Override
    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        Class<?> controllerClass = method.getDeclaringClass();
        APIVersion apiVersion = AnnotationUtils.findAnnotation(controllerClass, APIVersion.class);
        APIVersion methodAnnotation = AnnotationUtils.findAnnotation(method, APIVersion.class);
        //以方法上的注解优先
        if (methodAnnotation != null) {
            apiVersion = methodAnnotation;
        }

        String[] urlPatterns = apiVersion == null ? new String[0] : apiVersion.value();

        PatternsRequestCondition apiPattern = new PatternsRequestCondition(urlPatterns);
        PatternsRequestCondition oldPattern = mapping.getPatternsCondition();
        //拼接
        PatternsRequestCondition updatedFinalPattern = apiPattern.combine(oldPattern);
        mapping = new RequestMappingInfo(mapping.getName(), updatedFinalPattern, mapping.getMethodsCondition(),
                mapping.getParamsCondition(), mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                mapping.getProducesCondition(), mapping.getCustomCondition());
        super.registerHandlerMethod(handler, method, mapping);
    }
}