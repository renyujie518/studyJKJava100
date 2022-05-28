package org.geekbang.time.commonmistakes.collection.aslist;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
/**
 * @description 10-1  数组转list
 */
@Slf4j
public class AsListApplication {

    public static void main(String[] args) {

        wrong2();
        //right2();

    }

    private static void wrong1() {
        int[] arr = {1, 2, 3};
        List list = Arrays.asList(arr);
        log.info("list:{} size:{} class:{}", list, list.size(), list.get(0).getClass());
    }

    private static void right1() {
        int[] arr1 = {1, 2, 3};
        List list1 = Arrays.stream(arr1).boxed().collect(Collectors.toList());
        log.info("list:{} size:{} class:{}", list1, list1.size(), list1.get(0).getClass());

        Integer[] arr2 = {1, 2, 3};
        List list2 = Arrays.asList(arr2);
        log.info("list:{} size:{} class:{}", list2, list2.size(), list2.get(0).getClass());
    }


    private static void wrong2() {
        String[] arr = {"1", "2", "3"};
        List list = Arrays.asList(arr);
        //对原始数组的修改会影响到我们获得的那个 List
        arr[1] = "4";
        try {
            //Arrays.asList 返回的 List 不支持增删操作
            list.add("5");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        log.info("arr:{} list:{}", Arrays.toString(arr), list);
    }

    private static void right2() {
        String[] arr = {"1", "2", "3"};
        //重新 new 一个 ArrayList 初始化 Arrays.asList 返回的 List 避免和原始数组相互影响
        List list = new ArrayList(Arrays.asList(arr));
        arr[1] = "4";
        try {
            list.add("5");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        log.info("arr:{} list:{}", Arrays.toString(arr), list);
    }
}

