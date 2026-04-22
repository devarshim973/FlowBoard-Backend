package com.flowboard.comment_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name}")
    private String queue;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    /*
    This method creates a Queue in RabbitMQ.
    - Queue name is taken from application properties.
    - 'true' means the queue is durable.
    - Durable queue = data/messages will not be lost even if RabbitMQ restarts.
    */
    @Bean
    public Queue notificationQueue() {
        return new Queue(queue, true);
    }

    /*
    This method creates a Direct Exchange.
    - Exchange is responsible for receiving messages from producers.
    - It routes messages to the correct queue based on routing key.
    */
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(exchange);
    }

    /*
    This method binds the Queue with the Exchange.
    - Binding ensures messages sent to exchange go to the correct queue.
    - Routing key decides which messages should be routed to this queue.
    */
    @Bean
    public Binding notificationBinding(Queue activityQueue, DirectExchange activityExchange) {
        return BindingBuilder.bind(activityQueue).to(activityExchange).with(routingKey);
    }

    /*
    This method defines a Message Converter.
    - Converts Java objects into JSON format before sending to RabbitMQ.
    - Also converts JSON back to Java objects when consuming messages.
    - Avoids manual serialization/deserialization.
    */
    @Bean
    public MessageConverter jsonMessageConverter()   {
        return new Jackson2JsonMessageConverter();
    }
}