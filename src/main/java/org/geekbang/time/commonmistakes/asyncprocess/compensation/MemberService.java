package org.geekbang.time.commonmistakes.asyncprocess.compensation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @description   会员服务  监听用户注册成功的消息， 并发送欢迎短信
 */
@Component
@Slf4j
public class MemberService {

    //存放那些发过短信的用户 ID 和状态
    private Map<Long, Boolean> welcomeStatus = new ConcurrentHashMap<>();

    /**
     * @description 监听用户注册成功的消息，发送欢迎消息
     */
    @RabbitListener(queues = RabbitConfiguration.QUEUE)
    public void listen(User user) {
        log.info("receive mq user {}", user.getId());
        welcome(user);
    }

    public void welcome(User user) {
        /** putIfAbsent：先判断指定的键（key）是否存在，不存在则将键/值对插入到 HashMap 中
         * 如果所指定的 key 已经在 HashMap 中存在，返回和这个 key 值对应的 value
         * 如果所指定的 key 不在 HashMap 中存在，则返回 null **/
        //同一条消息可能既走 MQ 也走本次的补偿逻辑（补偿 Job 本身不会做去重处理），肯定会出现重复
        // 所以这里用putIfAbsent去重保证幂等  避免相同的用户id进行补偿时重复发送短信
        //即返回值为null代表key一定不存在  这时候再发送短信
        if (welcomeStatus.putIfAbsent(user.getId(), true) == null) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
            }
            //模拟发短信
            log.info("memberService: welcome new user {}", user.getId());
        }
    }
}
