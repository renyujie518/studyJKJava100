package org.geekbang.time.commonmistakes.apidesign.apiresponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description 如果某些 @RestController 的 接口不希望实现自动包装的话，可以标记这个注解：
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoAPIResponse {
}
