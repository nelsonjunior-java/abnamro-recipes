package com.abnamro.recipes_api.infra.messaging.impl;

import com.abnamro.recipes_api.infra.messaging.Message;
import com.abnamro.recipes_api.infra.messaging.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
@Slf4j
public class RabbitMQMessageSender implements MessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage(String queueName, Message message) {

        log.info("method=sendMessage, message=[{}]", message);

        rabbitTemplate.convertAndSend(queueName, message);
    }

}
