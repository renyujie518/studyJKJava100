package org.geekbang.time.commonmistakes.redundantcode.reflection.right;

import java.lang.annotation.*;
/**
 * @description 通过注解实现了对 API 参数的描述
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface BankAPI {
    //接口说明
    String desc() default "";

    //接口 URL 地址
    String url() default "";
}
