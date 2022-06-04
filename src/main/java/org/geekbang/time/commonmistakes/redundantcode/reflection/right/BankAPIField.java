package org.geekbang.time.commonmistakes.redundantcode.reflection.right;

import java.lang.annotation.*;
/**
 * @description 描述接口的每一个字段规范
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface BankAPIField {
    //参数次序
    int order() default -1;

    //参数长度
    int length() default -1;

    //参数类型
    String type() default "";
}
