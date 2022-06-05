package org.geekbang.time.commonmistakes.asyncprocess.compensation;

import jodd.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * @description 25-1 使用补偿 Job 定期进行消息丢失问题的补偿（备线操作）
 */
@Component
@Slf4j
public class CompensationJob {
    //补偿任务我们提交到线程池进行“异步”处理，提高处理能力
    private static ThreadPoolExecutor compensationThreadPool = new ThreadPoolExecutor(
            10, 10,
            1, TimeUnit.HOURS,
            new ArrayBlockingQueue<>(1000),
            new ThreadFactoryBuilder().setNameFormat("compensation-threadpool-%d").get());
    @Autowired
    private UserService userService;
    @Autowired
    private MemberService memberService;
    //目前补偿到哪个用户ID
    private long offset = 0;

    /**
     * @description  定义一个 @Scheduled 定时任务， 10秒后开始补偿  5 秒做一次全量补偿操作
     * @description  定义一个 @Scheduled 定时任务， 10秒后开始补偿  5 秒做一次全量补偿操作
     */
    @Scheduled(initialDelay = 10_000, fixedRate = 5_000)
    public void compensationJob() {
        log.info("开始从用户ID {} 补偿", offset);
        //按顺序一次补偿 5 个用户，下一次补偿操作从上一次补偿的最后一个用户 ID 开 始
        userService.getUsersAfterIdWithLimit(offset, 5).forEach(user -> {
            compensationThreadPool.execute(() -> memberService.welcome(user));
            offset = user.getId();
        });
    }
}
