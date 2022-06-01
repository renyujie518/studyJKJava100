package org.geekbang.time.commonmistakes.advancedfeatures.reflectionissue;

import lombok.extern.slf4j.Slf4j;
/**
 * @description 18-1  反射调用方法，是以反射获取方法时传入的方法名称和参数类型来确 定调用方法的
 */
@Slf4j
public class ReflectionIssueApplication {

    public static void main(String[] args) throws Exception {

        ReflectionIssueApplication application = new ReflectionIssueApplication();
        application.wrong();
        application.right();

    }

    private void age(int age) {
        log.info("int age = {}", age);
    }

    private void age(Integer age) {
        log.info("Integer age = {}", age);
    }

    /**
     * @description 传入的参数类型 Integer.TYPE 代表的是 int，
     */
    public void wrong() throws Exception {
        getClass().getDeclaredMethod("age", Integer.TYPE).invoke(this, Integer.valueOf("36"));
    }

    /**
     * @description Integer.class，执行的参数类型就是包装类型的 Integer
     */
    public void right() throws Exception {
        getClass().getDeclaredMethod("age", Integer.class).invoke(this, Integer.valueOf("36"));
        getClass().getDeclaredMethod("age", Integer.class).invoke(this, 36);
    }
}
