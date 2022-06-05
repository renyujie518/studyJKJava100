package org.geekbang.time.commonmistakes.asyncprocess.deadletter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
/**
 * @description 25-3 死信堆积问题
 */
@Configuration
@Slf4j
public class RabbitConfiguration {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Bean
    public Declarables declarables() {
        Queue queue = new Queue(Consts.QUEUE);
        DirectExchange directExchange = new DirectExchange(Consts.EXCHANGE);
        //快速声明一组对象，包含队列、交换器，以及队列到交换器的绑定
        return new Declarables(queue, directExchange,
                BindingBuilder.bind(queue).to(directExchange).with(Consts.ROUTING_KEY));
    }

    /**
     * @description 消息投递到专门的一个死信队列（这些都是普通的交换器和队列）
     */
    @Bean
    public Declarables declarablesForDead() {
        //定义死信交换器和队列，并且进行绑定
        Queue queue = new Queue(Consts.DEAD_QUEUE);
        DirectExchange directExchange = new DirectExchange(Consts.DEAD_EXCHANGE);
        return new Declarables(queue, directExchange,
                BindingBuilder.bind(queue).to(directExchange).with(Consts.DEAD_ROUTING_KEY));
    }

    /**
     * @description 用于处 理失败时候的重试
     */
    @Bean
    public RetryOperationsInterceptor interceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(5)//最多尝试（不是重试）5次 实际上是重试4次
                .backOffOptions(1000, 2.0, 10000)//指数退避重试（首次重试延迟 1 秒，第二次 2 秒，以此类推，1 秒、2 秒、4 秒、8 秒 最大延迟是 10 秒）
                //如果第 4 次重试 还是失败，则使用 RepublishMessageRecoverer 把消息重新投入一个“死信交换器”中。
                .recoverer(new RepublishMessageRecoverer(rabbitTemplate, Consts.DEAD_EXCHANGE, Consts.DEAD_ROUTING_KEY))
                .build();
    }

    /**
     * @description 通过定义SimpleRabbitListenerContainerFactory，设置其adviceChain属性为之前定义的RetryOperationsInterceptor
     * 默认情况下 SimpleMessageListenerContainer 只有一个消费线程。（所以msg2 是在 msg1 的四次重试全部结束后才开始处理。）
     * 可以通过增加消费线程来避免 性能问题，如下我们直接设置 concurrentConsumers 参数为 10，来增加到 10 个工作线程：
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(interceptor());
        factory.setConcurrentConsumers(10);
        return factory;
    }
}
