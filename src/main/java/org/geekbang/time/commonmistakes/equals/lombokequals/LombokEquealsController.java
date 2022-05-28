package org.geekbang.time.commonmistakes.equals.lombokequals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 08-4 lombok 的 @Data 注解会帮我们实现 equals 和 hashcode 方法，但是有继承关系时
 */
@RestController
@Slf4j
@RequestMapping("lombokequals")
public class LombokEquealsController {

    @GetMapping("test1")
    public void test1() {
        Person person1 = new Person("zhuye", "001");
        Person person2 = new Person("Joseph", "001");
        log.info("person1.equals(person2) ? {}", person1.equals(person2));
    }

    @GetMapping("test2")
    public void test2() {
        Employee employee1 = new Employee("zhuye", "001", "bkjk.com");
        Employee employee2 = new Employee("Joseph", "002", "bkjk.com");
        log.info("employee1.equals(employee2) ? {}", employee1.equals(employee2));
    }


    @Data
    class Person {
        //希望只要身份证一致就认为是同一个人的话，可以 使用 @EqualsAndHashCode.Exclude 注解来修饰 name 字段，
        //从 equals 和 hashCode 的实现中排除 name 字段：
        @EqualsAndHashCode.Exclude
        private String name;
        private String identity;

        public Person(String name, String identity) {
            this.name = name;
            this.identity = identity;
        }
    }

    @Data
    //@EqualsAndHashCode 默认实现没有使用父类属性。手动设置 callSuper 开关为 true，来覆盖这种默认行为
    //此时  父类中equals的判断依据是identity  此时继承过来  再加上子类本身中的company   两者一起作为 equals 和 hashCode 方法的实现条件
    @EqualsAndHashCode(callSuper = true)
    class Employee extends Person {

        private String company;

        public Employee(String name, String identity, String company) {
            super(name, identity);
            this.company = company;
        }
    }
}
