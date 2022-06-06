package org.geekbang.time.commonmistakes.clientdata.trustclientuserid;

import java.lang.annotation.*;

/**
 * @description 自定义注解 白术在userId 参数上
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface LoginRequired {
    String sessionKey() default "currentUser";
}