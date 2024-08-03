package com.abnamro.recipes_api.infra.messaging;

public interface MessageSender {

    void sendMessage(String queueName, Message message);
}
