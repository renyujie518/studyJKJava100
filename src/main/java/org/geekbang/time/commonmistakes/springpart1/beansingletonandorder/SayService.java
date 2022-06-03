package org.geekbang.time.commonmistakes.springpart1.beansingletonandorder;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @description 每次调用 say 方法都会往 data 加入新数据，可 以认为 SayService 是有状态，
 */
@Slf4j
public abstract class SayService {
    List<String> data = new ArrayList<>();

    public void say() {
        data.add(IntStream.rangeClosed(1, 1000000)
                .mapToObj(__ -> "a")
                .collect(Collectors.joining("")) + UUID.randomUUID().toString());
        log.info("I'm {} size:{}", this, data.size());
    }
}
