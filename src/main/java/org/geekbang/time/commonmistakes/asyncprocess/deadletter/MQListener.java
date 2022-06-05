package org.geekbang.time.commonmistakes.asyncprocess.deadletter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MQListener {

    /**
     * @description 收到消息后，直接抛出空指针异常，模拟处理出错的情况
     */
    @RabbitListener(queues = Consts.QUEUE)
    public void handler(String data) {
        //http://localhost:15672/#/
        log.info("got message {}", data);
        throw new NullPointerException("error");
        /** 在消费端最简单解决死信的方法时直接抛出 AmqpRejectAndDontRequeueException 异常，避免消息重新进入队列 **/
        //throw new AmqpRejectAndDontRequeueException("error");
    }

    /**
     * @description 死信队列处理程序  这里只是记录日志
     */
    @RabbitListener(queues = Consts.DEAD_QUEUE)
    public void deadHandler(String data) {
        log.error("got dead message {}", data);
    }
}
