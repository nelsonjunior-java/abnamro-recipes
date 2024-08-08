package com.abnamro.recipes_consumer.integration;

import com.abnamro.recipes_consumer.config.RabbitMQConfig;
import com.abnamro.recipes_consumer.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_consumer.infra.repository.IngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class IngredientMessageConsumerIntegrationTest extends BaseIntegrationTest {

    private static final String INGREDIENT_NAME = "Salt";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("abnamro")
            .withUsername("admin")
            .withPassword("admin");

    @Container
    private static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management");

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        ingredientRepository.deleteAll();
    }

    @Test
    public void testConsumeIngredientMessage_Success() {
        // Given
        final UUID uuid = UUID.randomUUID();
        final String ingredientName = "Salt";
        final IngredientMessageDTO message = new IngredientMessageDTO();
        message.setUuid(uuid);
        message.setName(ingredientName);

        rabbitTemplate.convertAndSend(RabbitMQConfig.INGREDIENT_QUEUE_NAME, message);

        // When
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                ingredientRepository.existsByUuid(uuid)
        );

        // Then
        assertTrue(ingredientRepository.existsByUuid(uuid), "The ingredient should be saved in the database.");
    }

    @Test
    public void testConsumeIngredientMessage_InvalidData() {
        // Given: Create an IngredientMessageDTO with invalid data (missing UUID and name)
        final IngredientMessageDTO invalidMessage = new IngredientMessageDTO();
        // intentionally not setting uuid and name to simulate invalid data

        // When: Send the invalid message to the queue
        rabbitTemplate.convertAndSend(RabbitMQConfig.INGREDIENT_QUEUE_NAME, invalidMessage);

        // Then: Ensure the message is not saved in the database by checking that no records exist
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
                ingredientRepository.findAll().isEmpty()
        );

        // The repository should still be empty because the invalid message should not have been saved
        assertTrue(ingredientRepository.findAll().isEmpty(), "No ingredients should be saved in the database with invalid data.");
    }


}
