package org.geekbang.time.commonmistakes.datetime.timezone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
/**
 * @description 16-1 国际时间问题
 */
public class CommonMistakesApplication {

    public static void main(String[] args) throws Exception {
        test();
        wrong1();
        wrong2();
        right();
    }

    /**
     * @description Date 中保存的是一个时间戳，代表的是从 1970 年 1 月 1 日 0 点（Epoch 时 间）到现在的毫秒数。
     */
    private static void test() {
        System.out.println("test");
        System.out.println(new Date(0));
        //System.out.println(TimeZone.getDefault().getID() + ":" + TimeZone.getDefault().getRawOffset()/3600/1000);
        //ZoneId.getAvailableZoneIds().forEach(id -> System.out.println(String.format("%s:%s", id, ZonedDateTime.now(ZoneId.of(id)))));
    }

/**
 * @description 不同时区的人转换成 Date 会得到不同的时间（时间戳）
 */
    private static void wrong1() throws ParseException {
        System.out.println("wrong1");
        String stringDate = "2020-01-02 22:00:00";
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //默认时区解析时间表示
        Date date1 = inputFormat.parse(stringDate);
        System.out.println(date1 + ":" + date1.getTime());  //1577973600000
        //纽约时区解析时间表示
        inputFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date2 = inputFormat.parse(stringDate);//1578020400000
        System.out.println(date2 + ":" + date2.getTime());
    }

    /**
     * @description ，有些时候数据库中相同的时间，由于服务器的时区设置不同，读取到的时间表示不 同
     */
    private static void wrong2() throws ParseException {
        System.out.println("wrong2");
        String stringDate = "2020-01-02 22:00:00";
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = inputFormat.parse(stringDate);
        System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss Z]").format(date));//[2020-01-02 22:00:00 +0800]
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
        System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss Z]").format(date));//[2020-01-02 09:00:00 -0500]
    }

    /**
     * @description Java 8 推出了新的时间日期类
     */
    private static void right() {
        System.out.println("right");

        String stringDate = "2020-01-02 22:00:00";
        ZoneId timeZoneSH = ZoneId.of("Asia/Shanghai");
        ZoneId timeZoneNY = ZoneId.of("America/New_York");
        //具有指定时间 差的自定义时区
        ZoneId timeZoneJST = ZoneOffset.ofHours(9);

        //使用DateTimeFormatter格式化时间，
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        //LocalDateTime 不带有时区属性，所以命名为本地时区的日期时 间
        //ZonedDateTime=LocalDateTime+ZoneId，具有时区属性。
        ZonedDateTime date = ZonedDateTime.of(LocalDateTime.parse(stringDate, dateTimeFormatter), timeZoneJST);

        //使用 DateTimeFormatter 格式化时间的时候，可以直接通过 withZone 方法直接设置 格式化使用的时区
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
        System.out.println(timeZoneSH.getId() + outputFormat.withZone(timeZoneSH).format(date));
        System.out.println(timeZoneNY.getId() + outputFormat.withZone(timeZoneNY).format(date));
        System.out.println(timeZoneJST.getId() + outputFormat.withZone(timeZoneJST).format(date));
    }

}

