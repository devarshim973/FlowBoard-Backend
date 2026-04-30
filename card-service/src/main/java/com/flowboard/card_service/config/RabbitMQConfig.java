package com.flowboard.card_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.single.name}")
    private String singleQueue;

    @Value("${rabbitmq.queue.bulk.name}")
    private String bulkQueue;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.single.key}")
    private String singleRoutingKey;

    @Value("${rabbitmq.routing.bulk.key}")
    private String bulkRoutingKey;

    /*
     Queue for rabbitmq, create queue with the name given in queue name be careful when importing
     the true here is for durable means if we restart so the messages in queue are not removed.
     */

    // If you wnat to specify bean name while creating use @Bean("singleQueueBean")
    @Bean
    public Queue singleNotificationQueue() {
        return new Queue(singleQueue, true);
    }

    @Bean
    public Queue bulkNotificationQueue() {
        return new Queue(bulkQueue, true);
    }

    /*
    create an exchange in the rabbitmq with the given exchange key with the name given
    and this will further route to the queue we created, here we are using direct exchange
    so the request will come to exchange and then according to the routing key it will
    directly route
     */
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(exchange);
    }

    /*
    bind the exchange with queue so if anythings comes to this exchange it will send
    the request to the queue.(binding queue and exchange)
     */
    @Bean
    public Binding singleNotificationBinding(@Qualifier("singleNotificationQueue") Queue singleNotificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(singleNotificationQueue).to(notificationExchange).with(singleRoutingKey);
    }

    @Bean
    public Binding bulkNotificationBinding(@Qualifier("bulkNotificationQueue") Queue bulkNotificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(bulkNotificationQueue).to(notificationExchange).with(bulkRoutingKey);
    }

    /*
     Convert java object to json before sending them to RabbitMQ, by default rabbitmq sends message as raw
     byte arrays and then you have to manually serialize and deserialize them.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}