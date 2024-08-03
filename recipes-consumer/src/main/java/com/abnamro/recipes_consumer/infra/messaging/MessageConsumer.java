package com.abnamro.recipes_consumer.infra.messaging;

public interface MessageConsumer <T extends Message>{

    void consumeMessage(T message);
}
