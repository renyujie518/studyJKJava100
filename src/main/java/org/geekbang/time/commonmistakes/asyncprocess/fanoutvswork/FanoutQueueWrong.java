package org.geekbang.time.commonmistakes.asyncprocess.fanoutvswork;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

/**
 * @description 25-2 广播模式和工作队列模式
 * ，同一个用户的注册消息，会员服务需要监听以发送欢迎短信，营销服务同样需要监听 以发送新用户小礼物
 * 同一个 用户的消息，可以同时广播给不同的服务（广播模式），
 * 但对于同一个服务的不同实例（比 如会员服务 1 和会员服务 2），不管哪个实例来处理，处理一次即可（工作队列模式）
 */
@Slf4j
//@Configuration
//@RestController
@RequestMapping("fanoutwrong")
public class FanoutQueueWrong {
    private static final String QUEUE = "newuser";
    private static final String EXCHANGE = "newuser";
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping
    public void sendMessage() {
        rabbitTemplate.convertAndSend(EXCHANGE, "", UUID.randomUUID().toString());
    }

    /**
     * @description 声明FanoutExchange，然后绑定到队列，FanoutExchange绑定队列的时候不需要routingKey
     */
    @Bean
    public Declarables declarables() {
        Queue queue = new Queue(QUEUE);
        //广播交换器 FanoutExchange ，广播消息到所有绑定的队列
        FanoutExchange exchange = new FanoutExchange(EXCHANGE);
        return new Declarables(queue, exchange,
                BindingBuilder.bind(queue).to(exchange));
    }

    /**
     * @description 绑定了同一个队列，所以这四个服务只能收到一次 消息
     */
    @RabbitListener(queues = QUEUE)
    public void memberService1(String userName) {
        log.info("memberService1: welcome message sent to new user {}", userName);

    }

    @RabbitListener(queues = QUEUE)
    public void memberService2(String userName) {
        log.info("memberService2: welcome message sent to new user {}", userName);

    }

    @RabbitListener(queues = QUEUE)
    public void promotionService1(String userName) {
        log.info("promotionService1: gift sent to new user {}", userName);
    }

    @RabbitListener(queues = QUEUE)
    public void promotionService2(String userName) {
        log.info("promotionService2: gift sent to new user {}", userName);
    }
}
