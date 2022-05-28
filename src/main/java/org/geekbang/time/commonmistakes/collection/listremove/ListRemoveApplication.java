package org.geekbang.time.commonmistakes.collection.listremove;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/**
 * @description 10-5  课后习题 List的remove问题
 */
public class ListRemoveApplication {

    public static void main(String[] args) {
//        removeByIndex(4);
//        removeByValue(Integer.valueOf(4));

        forEachRemoveWrong();
        forEachRemoveRight();
        forEachRemoveRight2();
    }


    /**
     * @description ArrayList 的 remove方法，参是int类型的话，表示的是删除相对应索引位置的元素。
     */
    private static void removeByIndex(int index) {
        List<Integer> list =
                IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toCollection(ArrayList::new));
        System.out.println(list.remove(index));
        System.out.println(list);
    }

    /**
     * @description ArrayList 的 remove方法，如果传参是 Integer类型的话，表示的是删除 元素
     */
    private static void removeByValue(Integer index) {
        List<Integer> list =
                IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toCollection(ArrayList::new));
        System.out.println(list.remove(index));
        System.out.println(list);
    }

    /**
     * @description 循环遍历 List，调用 remove 方法删除元素，往往会遇到 ConcurrentModificationException 异常
     * 原因：使用 for-each 或者 iterator 进行迭代删除 remove 时，
     * 容易导致 next() 检测的 modCount 不等于 expectedModCount 从而引发 ConcurrentModificationExcept ion。
     */
    private static void forEachRemoveWrong() {
        List<String> list =
                IntStream.rangeClosed(1, 10).mapToObj(String::valueOf).collect(Collectors.toCollection(ArrayList::new));
        for (String i : list) {
            if ("2".equals(i)) {
                list.remove(i);
            }
        }
        System.out.println(list);
    }

/**
 * @description 循环遍历 List，调用 remove 方法删除元素
 * 在单线程下，推荐使用 next() 得到元素，然后直接调用 remove(),注意是无参的 remove; 多线程情况下还是使用并发容器吧
 */
    private static void forEachRemoveRight() {
        List<String> list =
                IntStream.rangeClosed(1, 10).mapToObj(String::valueOf).collect(Collectors.toCollection(ArrayList::new));
        for (Iterator<String> iterator = list.iterator(); iterator.hasNext(); ) {
            String next = iterator.next();
            if ("2".equals(next)) {
                iterator.remove();
            }
        }
        System.out.println(list);

    }

    /**
     * @description 循环遍历 List，调用 remove 方法删除元素
     * 也可以使用安全的removeIf
     */
    private static void forEachRemoveRight2() {
        List<String> list =
                IntStream.rangeClosed(1, 10).mapToObj(String::valueOf).collect(Collectors.toCollection(ArrayList::new));
        list.removeIf(item -> item.equals("2"));
        System.out.println(list);
    }
}

