package com.abnamro.recipes_api.unit.infra.messaging.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.abnamro.recipes_api.controller.request.CreateIngredientRequest;
import com.abnamro.recipes_api.infra.messaging.Message;
import com.abnamro.recipes_api.infra.messaging.dto.IngredientMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import com.abnamro.recipes_api.infra.messaging.impl.RabbitMQMessageSender;

public class RabbitMQMessageSenderTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQMessageSender rabbitMQMessageSender;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendMessage_ShouldSendMessageToCorrectQueue() {
        // Arrange
        final String queueName = "test-queue";
        final Message mockMessage = new Message() {
            // Implementing a simple Message object for testing
        };

        // Act
        rabbitMQMessageSender.sendMessage(queueName, mockMessage);

        // Assert
        final ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        verify(rabbitTemplate).convertAndSend(queueNameCaptor.capture(), messageCaptor.capture());

        assertEquals(queueName, queueNameCaptor.getValue());
        assertEquals(mockMessage, messageCaptor.getValue());
    }

    @Test
    public void testSendMessage_LogsMessage() {

        final String queueName = "test-queue";

        // Given
        final CreateIngredientRequest createIngredientRequest = new CreateIngredientRequest();
        createIngredientRequest.setName("Salt");

        final IngredientMessageDTO mockMessage = IngredientMessageDTO.of(createIngredientRequest);

        // Capture logs using Logback's ListAppender
        final ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RabbitMQMessageSender.class);

        final ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        // When
        rabbitMQMessageSender.sendMessage(queueName, mockMessage);

        // Then
        final List<ch.qos.logback.classic.spi.ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty(), "Expected a log message, but no log was found.");
        final String actualLogMessage = logsList.get(0).getFormattedMessage();
        final String expectedLogMessage = String.format("method=sendMessage, message=[%s]", mockMessage);

        assertEquals(expectedLogMessage, actualLogMessage);

        // Clean up
        logger.detachAppender(listAppender);
    }

}
