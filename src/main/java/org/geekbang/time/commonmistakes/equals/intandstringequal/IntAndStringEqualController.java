package org.geekbang.time.commonmistakes.equals.intandstringequal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @description 08-1  equals 和 == 的区别
 */
@RestController
@Slf4j
@RequestMapping("intandstringequal")
public class IntAndStringEqualController {

    List<String> list = new ArrayList<>();

    @GetMapping("stringcompare")
    public void stringcomare() {
        String a = "1";
        String b = "1";
        log.info("\nString a = \"1\";\n" +
                "String b = \"1\";\n" +
                "a == b ? {}", a == b); //true   Java 的字符串驻留机制，直接使用双引号声明出来的两个 String 对象指向常量池中的相同字符串。

        String c = new String("2");
        String d = new String("2");
        log.info("\nString c = new String(\"2\");\n" +
                "String d = new String(\"2\");" +
                "c == d ? {}", c == d); //false    new 出来的两个 String 是不同对象，引用当然不同

        String e = new String("3").intern();
        String f = new String("3").intern();
        log.info("\nString e = new String(\"3\").intern();\n" +
                "String f = new String(\"3\").intern();\n" +
                "e == f ? {}", e == f); //true 使用 String 提供的 intern 方法也会走常量池机制

        String g = new String("4");
        String h = new String("4");
        log.info("\nString g = new String(\"4\");\n" +
                "String h = new String(\"4\");\n" +
                "g == h ? {}", g.equals(h)); //true
    }

    /**
     * @description 验证intern的性能
     */
    @GetMapping("internperformance")
    public int internperformance(@RequestParam(value = "size", defaultValue = "10000000") int size) {
        //-XX:+PrintStringTableStatistics
        //-XX:StringTableSize=10000000
        long begin = System.currentTimeMillis();
        list = IntStream.rangeClosed(1, size)
                .mapToObj(i -> String.valueOf(i).intern())
                .collect(Collectors.toList());
        log.info("size:{} took:{}", size, System.currentTimeMillis() - begin);
        return list.size();
    }

    /**
     * @description 总结的宗旨就是
     * 比较 Integer 的值请使用 equals，而不是 ==
     * 对于基本类型 int 的比较当然只 能使用 ==
     */
    @GetMapping("intcompare")
    public void intcompare() {

        Integer a = 127; //Integer.valueOf(127)
        Integer b = 127; //Integer.valueOf(127)
        log.info("\nInteger a = 127;\n" +
                "Integer b = 127;\n" +
                "a == b ? {}", a == b);    // true  编译器会把 Integer a = 127 转换为 Integer.valueOf(127)
        //这个转换在内部其实做了缓存，使得两个 Integer 指向同一个对象

        Integer c = 128; //Integer.valueOf(128)
        Integer d = 128; //Integer.valueOf(128)
        log.info("\nInteger c = 128;\n" +
                "Integer d = 128;\n" +
                "c == d ? {}", c == d);   //false  默认情况下会缓存[-128, 127]的数值，而 128 处于这个区间之外
        //设置-XX:AutoBoxCacheMax=1000再试试

        Integer e = 127; //Integer.valueOf(127)
        Integer f = new Integer(127); //new instance
        log.info("\nInteger e = 127;\n" +
                "Integer f = new Integer(127);\n" +
                "e == f ? {}", e == f);   //false  New 出来的 Integer 始终是不走缓存的新对象

        Integer g = new Integer(127); //new instance
        Integer h = new Integer(127); //new instance
        log.info("\nInteger g = new Integer(127);\n" +
                "Integer h = new Integer(127);\n" +
                "g == h ? {}", g == h);  //false   比较两个新对象返回false

        Integer i = 128; //unbox
        int j = 128;
        log.info("\nInteger i = 128;\n" +
                "int j = 128;\n" +
                "i == j ? {}", i == j); //true   装箱的 Integer 和基本类型 int 比较，前者会先拆箱再比较，比较的 肯定是数值

    }


    @PostMapping("enumcompare")
    public void enumcompare(@RequestBody OrderQuery orderQuery) {
        StatusEnum statusEnum = StatusEnum.DELIVERED;
        log.info("orderQuery:{} statusEnum:{} result:{}", orderQuery, statusEnum, statusEnum.status == orderQuery.getStatus());
    }

    enum StatusEnum {
        CREATED(1000, "已创建"),
        PAID(1001, "已支付"),
        DELIVERED(1002, "已送到"),
        FINISHED(1003, "已完成");

        private final Integer status;
        private final String desc;

        StatusEnum(Integer status, String desc) {
            this.status = status;
            this.desc = desc;
        }
    }
}
