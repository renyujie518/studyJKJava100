package org.geekbang.time.commonmistakes.java8;

import org.junit.Test;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CoolStreamTest {

    private Map<Long, Product> cache = new ConcurrentHashMap<>();

    /**
     * @description
     * 把整数列表转换为 Point2D 列表；
     *
     * 遍历 Point2D 列表过滤出 Y 轴 >1 的对象；
     *
     * 计算 Point2D 点到原点的距离； 累加所有计算出的距离，并计算距离的平均值
     */
    private static double calc(List<Integer> ints) {
        //临时中间集合
        List<Point2D> point2DList = new ArrayList<>();
        for (Integer i : ints) {
            point2DList.add(new Point2D.Double((double) i % 3, (double) i / 3));
        }
        //临时变量,纯粹是为了获得最后结果需要的中间变量
        double total = 0;
        int count = 0;

        for (Point2D point2D : point2DList) {
            //过滤
            if (point2D.getY() > 1) {
                //算距离
                double distance = point2D.distance(0, 0);
                total += distance;
                count++;
            }
        }
        return count > 0 ? total / count : 0;
    }

    @Test
    public void stream() {
        List<Integer> ints = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        double average = calc(ints);
        double streamResult = ints.stream()
                .map(i -> new Point2D.Double((double) i % 3, (double) i / 3))
                .filter(point -> point.getY() > 1)
                .mapToDouble(point -> point.distance(0, 0))
                .average()
                .orElse(0);
        //如何用一行代码来实现,比较一下可读性
        assertThat(average, is(streamResult));
    }

    @Test
    public void coolCache() //一条语句实现cache的常用模式
    {
        getProductAndCacheCool(1L);
        getProductAndCacheCool(100L);

        System.out.println(cache);
        assertThat(cache.size(), is(1));
        assertTrue(cache.containsKey(1L));
    }

    @Test
    public void notcoolCache() {
        getProductAndCache(1L);
        getProductAndCache(100L);

        System.out.println(cache);
        assertThat(cache.size(), is(1));
        assertTrue(cache.containsKey(1L));
    }

    /**
     * @description HashMap 实现一个缓存的操作
     */
    private Product getProductAndCacheCool(Long id) {
        //computeIfAbsent() 方法对 hashMap 中指定 key 的值进行重新计算，如果不存在这个 key，则添加到 hashMap 中。
        //如果 key 对应的 value 不存在，则使用获取 remappingFunction 重新计算后的值，并保存为该 key 的 value
        //如果key存在,不会有任何影响(在计算过程中，不允许使用此方法修改已有映射。)

        return cache.computeIfAbsent(id, i -> //当Key不存在的时候提供一个Function来代表根据Key获取Value的过程
                Product.getData().stream()
                        .filter(p -> p.getId().equals(i)) //过滤（此时的i就是第一个参数key）
                        .findFirst() //找第一个，得到Optional<Product>
                        .orElse(null)); //如果找不Product到则使用null 映射函数返回null，则不记录映射
    }

    private Product getProductAndCache(Long id) {
        Product product = null;
        if (cache.containsKey(id)) {
            product = cache.get(id);
        } else {
            for (Product p : Product.getData()) {
                if (p.getId().equals(id)) {
                    product = p;
                    break;
                }
            }
            if (product != null)
                cache.put(id, product);
        }
        return product;
    }

    /**
     * @description ：递归搜索文件夹，查找所有的.java 文件；
     * 然后读取文件每一行内容， 用正则表达式匹配 public class 关键字；
     * 最后输出文件名和这行内容
     */
    @Test
    public void filesExample() throws IOException {
        //无限深度，递归遍历文件夹
        try (Stream<Path> pathStream = Files.walk(Paths.get("."))) {
            pathStream.filter(Files::isRegularFile) //只查普通文件
                    .filter(FileSystems.getDefault().getPathMatcher("glob:**/*.java")::matches) //搜索java源码文件
                    //用 ThrowingFunction 包装 这个Files.readAllLines 方法(该方法使用时会抛出一个受检异常 （IOException））
                    //把受检异常（IO异常）转换为运行时异常
                    .flatMap(ThrowingFunction.unchecked(path ->
                            Files.readAllLines(path).stream() //读取文件内容，转换为Stream<List>
                                    .filter(line -> Pattern.compile("public class").matcher(line).find()) //使用正则过滤带有public class的行
                                    .map(line -> path.getFileName() + " >> " + line))) //把这行文件内容转换为文件名+行
                    .forEach(System.out::println); //打印所有的行
        }
    }

    @Test
    public void fibonacci() {
        Stream.iterate(new BigInteger[]{BigInteger.ONE, BigInteger.ONE},
                p -> new BigInteger[]{p[1], p[0].add(p[1])})
                .limit(100)
                .forEach(p -> System.out.println(p[0]));
    }


    /**
     * @description 使用了一个自定义的函数式接口，用 ThrowingFunction 包装 这个方法，把受检异常转换为运行时异常
     */
    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Throwable> {
        static <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> f) {
            return t -> {
                try {
                    return f.apply(t);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
        }

        R apply(T t) throws E;
    }
}
