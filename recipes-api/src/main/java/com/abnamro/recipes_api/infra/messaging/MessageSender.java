package com.abnamro.recipes_api.infra.messaging;

/**
 * Interface for sending messages to a messaging queue.
 * <p>
 * Implementations of this interface are responsible for delivering messages to a specified queue.
 * </p>
 */
public interface MessageSender {

    /**
     * Sends a message to the specified queue.
     *
     * @param queueName the name of the queue to which the message should be sent
     * @param message   the message to be sent
     */
    void sendMessage(String queueName, Message message);
}
