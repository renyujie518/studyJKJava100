package org.geekbang.time.commonmistakes.asyncprocess.fanoutvswork;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

//为了代码简洁直观，我们把消息发布者、消费者、以及MQ的配置代码都放在了一起
@Slf4j
//@Configuration
//@RestController
@RequestMapping("workqueuewrong")
public class WorkQueueWrong {

    private static final String EXCHANGE = "newuserExchange";
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /** 生产者 **/
    @GetMapping
    public void sendMessage() {
        rabbitTemplate.convertAndSend(EXCHANGE, "", UUID.randomUUID().toString());
    }


    /** MQ配置 **/
    //使用匿名队列(随机命名的队列)作为消息队列
    @Bean
    public Queue queue() {
        return new AnonymousQueue();
    }
    //声明DirectExchange交换器，绑定队列到交换器
    //没有理清楚 RabbitMQ 直接交换器和队列的绑定关系。
    @Bean
    public Declarables declarables() {
        DirectExchange exchange = new DirectExchange(EXCHANGE);
        return new Declarables(queue(), exchange,
                BindingBuilder.bind(queue()).to(exchange).with(""));
    }

    /** 消费者**/
    //监听队列，队列名称直接通过SpEL表达式引用Bean
    @RabbitListener(queues = "#{queue.name}")
    public void memberService(String userName) {
        log.info("memberService: welcome message sent to new user {} from {}", userName, System.getProperty("server.port"));

    }
}
