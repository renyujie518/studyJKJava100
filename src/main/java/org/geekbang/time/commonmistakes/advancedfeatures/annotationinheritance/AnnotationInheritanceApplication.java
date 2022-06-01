package org.geekbang.time.commonmistakes.advancedfeatures.annotationinheritance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
/**
 * @description 18-3 继承父类和父类方法上的注解问题
 */
@Slf4j
public class AnnotationInheritanceApplication {

    public static void main(String[] args) throws NoSuchMethodException {
        wrong();
        right();
    }

    /**
     * @description 输出注解的 value 属性的值（如果注解不存在则输出空字符串）
     */
    private static String getAnnotationValue(MyAnnotation annotation) {
        if (annotation == null) return "";
        return annotation.value();
    }

    public static void wrong() throws NoSuchMethodException {
        //获取父类的类和方法上的注解
        Parent parent = new Parent();
        log.info("ParentClass:{}", getAnnotationValue(parent.getClass().getAnnotation(MyAnnotation.class)));
        log.info("ParentMethod:{}", getAnnotationValue(parent.getClass().getMethod("foo").getAnnotation(MyAnnotation.class)));


        //获取子类的类和方法上的注解
        Child child = new Child();
        log.info("ChildClass:{}", getAnnotationValue(child.getClass().getAnnotation(MyAnnotation.class)));
        log.info("ChildMethod:{}", getAnnotationValue(child.getClass().getMethod("foo").getAnnotation(MyAnnotation.class)));

    }

    /**
     * @description AnnotatedElementUtils的 findMergedAnnotation 工具方法，
     * 可以帮助我们找出父类和接口、父类方法和接口方法上的注解，并可以处理桥接方法，实现一键找到继承链的注解
     */
    public static void right() throws NoSuchMethodException {
        Parent parent = new Parent();
        log.info("ParentClass:{}", getAnnotationValue(parent.getClass().getAnnotation(MyAnnotation.class)));
        log.info("ParentMethod:{}", getAnnotationValue(parent.getClass().getMethod("foo").getAnnotation(MyAnnotation.class)));

        Child child = new Child();
        log.info("ChildClass:{}", getAnnotationValue(AnnotatedElementUtils.findMergedAnnotation(child.getClass(), MyAnnotation.class)));
        log.info("ChildMethod:{}", getAnnotationValue(AnnotatedElementUtils.findMergedAnnotation(child.getClass().getMethod("foo"), MyAnnotation.class)));

    }

    @MyAnnotation(value = "Class")
    @Slf4j
    static class Parent {

        @MyAnnotation(value = "Method")
        public void foo() {
        }
    }

    @Slf4j
    static class Child extends Parent {
        @Override
        public void foo() {
        }
    }
}

