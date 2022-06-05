package org.geekbang.time.commonmistakes.asyncprocess.compensation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@RestController
@Slf4j
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("register")
    public void register() {
        //一次性注册 10 个用户
        IntStream.rangeClosed(1, 10).forEach(i -> {
            //落库
            User user = userService.register();
            //用户注册消息不能发送出去的概率为 50%  通过RabbitMQ发送user消息
            if (ThreadLocalRandom.current().nextInt(10) % 2 == 0) {
                rabbitTemplate.convertAndSend(RabbitConfiguration.EXCHANGE, RabbitConfiguration.ROUTING_KEY, user);
                log.info("sent mq user {}", user.getId());
            }
        });
    }
}
