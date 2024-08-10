package com.abnamro.recipes_api.infra.messaging.impl;

import com.abnamro.recipes_api.infra.messaging.Message;
import com.abnamro.recipes_api.infra.messaging.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Implementation of the {@link MessageSender} interface using RabbitMQ.
 * <p>
 * This service is responsible for sending messages to a specified RabbitMQ queue.
 * </p>
 */
@Service
@Slf4j
public class RabbitMQMessageSender implements MessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Sends a message to the specified RabbitMQ queue.
     *
     * @param queueName the name of the RabbitMQ queue to which the message should be sent
     * @param message   the message to be sent; it must be serializable
     */
    @Override
    public void sendMessage(String queueName, Message message) {

        log.info("method=sendMessage, message=[{}]", message);

        rabbitTemplate.convertAndSend(queueName, message);
    }

}
