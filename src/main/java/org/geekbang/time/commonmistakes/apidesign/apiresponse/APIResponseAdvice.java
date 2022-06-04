package org.geekbang.time.commonmistakes.apidesign.apiresponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @description  定义一个 @RestControllerAdvice 来完成自动包装响应体的工作
 * 目的是在业务代码中不需要考虑响应体的包装，代码会更简洁。
 */
@RestControllerAdvice
@Slf4j
public class APIResponseAdvice implements ResponseBodyAdvice<Object> {
    @Autowired
    private ObjectMapper objectMapper;

    //自动处理APIException，包装为APIResponse(自定义异常)
    @ExceptionHandler(APIException.class)
    public APIResponse handleApiException(HttpServletRequest request, APIException ex) {
        log.error("process url {} failed", request.getRequestURL().toString(), ex);
        APIResponse apiResponse = new APIResponse();
        apiResponse.setSuccess(false);
        apiResponse.setCode(ex.getErrorCode());
        apiResponse.setMessage(ex.getErrorMessage());
        return apiResponse;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public APIResponse handleException(NoHandlerFoundException ex) {
        log.error(ex.getMessage(), ex);
        APIResponse apiResponse = new APIResponse();
        apiResponse.setSuccess(false);
        apiResponse.setCode(4000);
        apiResponse.setMessage(ex.getMessage());
        return apiResponse;
    }

    /**
     * @param returnType 响应的数据类型
     * @param converterType 最终将会使用的消息转换器
     * @return 返回bool，表示是否要在响应之前执行“beforeBodyWrite” 方法
    boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType);
     */
    //仅当方法或类没有标记@NoAPIResponse（自定义注解）才自动包装
    //因为标记了@NoAPIResponse注解的意思是该@RestController的接口不希望实现自动包装
    //所以一旦有了@NoAPIResponse标记  就排除了标记有这个注解的方法或类的 自动响应体包装  即返回false
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return returnType.getParameterType() != APIResponse.class
                && AnnotationUtils.findAnnotation(returnType.getMethod(), NoAPIResponse.class) == null
                && AnnotationUtils.findAnnotation(returnType.getDeclaringClass(), NoAPIResponse.class) == null;
    }


    /**
     * @param body 响应的数据，也就是响应体
     * @param returnType 响应的数据类型
     * @param selectedContentType 响应的ContentType
     * @param selectedConverterType 最终将会使用的消息转换器
     * @param request
     * @param response
     * @return 被修改后的响应体，可以为null，表示没有任何响应
     @Nullable
     T beforeBodyWrite(@Nullable T body, MethodParameter returnType, MediaType selectedContentType,
     Class<? extends HttpMessageConverter<?>> selectedConverterType,
     ServerHttpRequest request, ServerHttpResponse response);
     **/
    //自动包装外层APIResposne响应
    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        APIResponse apiResponse = new APIResponse();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("OK");
        apiResponse.setCode(2000);
        apiResponse.setData(body);
        if (body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return objectMapper.writeValueAsString(apiResponse);
        } else {
            return apiResponse;
        }

    }
}
