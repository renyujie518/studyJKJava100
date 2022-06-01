package org.geekbang.time.commonmistakes.datetime.dateformat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/**
 * @description 16-2 最好使用Java 8 中的 DateTimeFormatter做日期格式转换和解析
 */
public class CommonMistakesApplication {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static ThreadLocal<SimpleDateFormat> threadSafeSimpleDateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    /**
     * @description  DateTimeFormatter
     * 来定义格式化字符串，不用去记忆使用 大写的 Y 还是小写的 Y，大写的 M 还是小写的 m
     * DateTimeFormatter 是线程安全的，可以定义为 static 使用；
     * DateTimeFormatter 的解析比较严格，需要解析的字符串和格式不匹配时，会直接报错，
     */
    private static DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR)
            .appendLiteral("/")
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral("/")
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral(" ")
            .appendValue(ChronoField.HOUR_OF_DAY)
            .appendLiteral(":")
            .appendValue(ChronoField.MINUTE_OF_HOUR)
            .appendLiteral(":")
            .appendValue(ChronoField.SECOND_OF_MINUTE)
            .appendLiteral(".")
            .appendValue(ChronoField.MILLI_OF_SECOND)
            .toFormatter();

    public static void main(String[] args) throws Exception {

        wrong1();
    }

    private static void test() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        System.out.println(simpleDateFormat.format(calendar.getTime()));
        System.out.println(dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())));
        System.out.println(dateTimeFormatter.format(LocalDateTime.now()));
    }

    private static void wrong1() throws ParseException {
        //三个问题，YYYY、线程不变安全、不合法格式

        Locale.setDefault(Locale.FRANCE);
        System.out.println("defaultLocale:" + Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, Calendar.DECEMBER, 29, 0, 0, 0);
        //使用大写的 YYYY 来初始 化 SimpleDateFormat
        SimpleDateFormat YYYY = new SimpleDateFormat("YYYY-MM-dd");
        System.out.println("格式化: " + YYYY.format(calendar.getTime()));
        System.out.println("weekYear:" + calendar.getWeekYear());
        System.out.println("firstDayOfWeek:" + calendar.getFirstDayOfWeek());
        System.out.println("minimalDaysInFirstWeek:" + calendar.getMinimalDaysInFirstWeek());

        String dateString = "20160901";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        System.out.println("result:" + dateFormat.parse(dateString));

    }

    /**
     * @description 没有特殊需求，针对年份的日期格式化，应该一律使用 “y” 而非 “Y”
     */
    private static void wrong1fix() throws ParseException {
        SimpleDateFormat yyyy = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, Calendar.DECEMBER, 29, 23, 24, 25);
        System.out.println("格式化: " + yyyy.format(calendar.getTime()));

        String dateString = "20160901";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        System.out.println("result:" + dateFormat.parse(dateString));
    }

/**
 * @description 使用Java 8 中的 DateTimeFormatter
 */
    private static void better() {
        LocalDateTime localDateTime = LocalDateTime.parse("2020/1/2 12:34:56.789", dateTimeFormatter);
        System.out.println(localDateTime.format(dateTimeFormatter));

        String dt = "20160901";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM");
        System.out.println("result:" + dateTimeFormatter.parse(dt));
    }

    /**
     * @description 定义的 static 的 SimpleDateFormat 可能会出现线程安全问题
     */
    private static void wrong2() throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(100);
        ////提交20个并发解析时间的任务到线程池，模拟并发环境
        for (int i = 0; i < 20; i++) {
            threadPool.execute(() -> {
                for (int j = 0; j < 10; j++) {
                    try {
                        System.out.println(simpleDateFormat.parse("2020-01-01 11:12:13"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);

    }

    /**
     * @description 通过 ThreadLocal 来存放 SimpleDateFormat
     * threadSafeSimpleDateFormat
     */
    private static void wrong2fix() throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(100);

        for (int i = 0; i < 20; i++) {
            threadPool.execute(() -> {
                for (int j = 0; j < 10; j++) {
                    try {
                        System.out.println(threadSafeSimpleDateFormat.get().parse("2020-01-01 11:12:13"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);
    }
}

