package com.abnamro.recipes_consumer.integration;

import com.abnamro.recipes_consumer.config.RabbitMQConfig;
import com.abnamro.recipes_consumer.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_consumer.infra.messaging.dto.RecipeMessageDTO;
import com.abnamro.recipes_consumer.infra.repository.IngredientRepository;
import com.abnamro.recipes_consumer.infra.repository.RecipeRepository;
import com.abnamro.recipes_consumer.model.Ingredients;
import com.abnamro.recipes_consumer.model.Recipes;
import com.abnamro.recipes_consumer.service.IngredientsService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class RecipeMessageConsumerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

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
        recipeRepository.deleteAll();
        ingredientRepository.deleteAll();


    }

    @Test
    public void testConsumeRecipeMessage_Success() {
        // Given
        UUID ingredientUuid = UUID.randomUUID();
        Ingredients ingredient = new Ingredients();
        ingredient.setUuid(ingredientUuid);
        ingredient.setName("Salt");

        // Save the ingredient in the database
        ingredientRepository.saveAndFlush(ingredient);

        // Prepare the recipe message
        final UUID recipeUuid = UUID.randomUUID();
        final String recipeName = "Test Recipe";
        final Integer servings = 4;
        final String instructions = "Cook for 20 minutes.";
        final Boolean vegetarian = true;

        IngredientMessageDTO ingredientMessageDTO = new IngredientMessageDTO();
        ingredientMessageDTO.setUuid(ingredientUuid);
        ingredientMessageDTO.setName("Salt");

        final RecipeMessageDTO message = new RecipeMessageDTO();
        message.setId(recipeUuid);
        message.setName(recipeName);
        message.setServings(servings);
        message.setInstructions(instructions);
        message.setVegetarian(vegetarian);
        message.setIngredientIds(Arrays.asList(ingredientMessageDTO));

        // When: Send the valid message to the queue
        rabbitTemplate.convertAndSend(RabbitMQConfig.RECIPE_QUEUE_NAME, message);

        // Then: Await until the message is processed and the recipe is saved in the database
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() ->
                recipeRepository.existsByUuid(recipeUuid)
        );

        // Fetch the saved recipe
        Recipes savedRecipe = recipeRepository.findByUuid(recipeUuid);

        // Verify that the recipe was saved in the database
        assertEquals(recipeName, savedRecipe.getName(), "The recipe name should match.");
        assertEquals(servings, savedRecipe.getServings(), "The recipe servings should match.");
        assertEquals(instructions, savedRecipe.getInstructions(), "The recipe instructions should match.");
        assertEquals(vegetarian, savedRecipe.isVegetarian(), "The recipe vegetarian flag should match.");
    }


    @Test
    public void testSaveAndRetrieveIngredient() {
        // Given
        UUID ingredientUuid = UUID.randomUUID();
        Ingredients ingredient = new Ingredients();
        ingredient.setUuid(ingredientUuid);
        ingredient.setName("Test Ingredient");

        // When: Save the ingredient to the repository
        ingredientRepository.save(ingredient);

        // Then: Retrieve the ingredient from the repository
        Optional<Ingredients> retrievedIngredient = ingredientRepository.findById(ingredient.getId());

        // Verify that the ingredient was saved and retrieved correctly
        assertTrue(retrievedIngredient.isPresent(), "Ingredient should be present in the repository");
        assertEquals("Test Ingredient", retrievedIngredient.get().getName(), "Ingredient name should match");
        assertEquals(ingredientUuid, retrievedIngredient.get().getUuid(), "Ingredient UUID should match");
    }
}
