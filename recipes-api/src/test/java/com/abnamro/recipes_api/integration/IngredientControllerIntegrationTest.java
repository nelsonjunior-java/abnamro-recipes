package com.abnamro.recipes_api.integration;

import com.abnamro.recipes_api.controller.reponse.IngredientResponse;
import com.abnamro.recipes_api.controller.request.CreateIngredientRequest;
import com.abnamro.recipes_api.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_api.infra.repository.IngredientRepository;
import com.abnamro.recipes_api.model.Ingredients;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.UUID;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@Slf4j
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IngredientControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String BASE_URL = "/api/v1/ingredient";
    private static final String INGREDIENTS_QUEUE = "ingredients_queue";

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
    public void testCreateIngredient_Success() throws Exception {

        //Creates request
        CreateIngredientRequest request = new CreateIngredientRequest();
        request.setName("Salt");

        final String responseContent = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(header().exists("Location"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String ingredientUuid = JsonPath.read(responseContent, "$.uuid");

        // Validates that a valid UUID format is being returned
        assertNotNull(ingredientUuid);
        assertDoesNotThrow(() -> UUID.fromString(ingredientUuid));
    }

    @Test
    public void testCreateIngredient_ShouldProduceMessageInTheQueue() throws Exception {

        // Creates request
        CreateIngredientRequest request = new CreateIngredientRequest();
        request.setName("Salt");

        final String responseContent = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(header().exists("Location"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final String ingredientUuid = JsonPath.read(responseContent, "$.uuid");

        // Validates that a valid UUID format is being returned
        assertNotNull(ingredientUuid);
        assertDoesNotThrow(() -> UUID.fromString(ingredientUuid));

        Awaitility.await()
                .atMost(50, TimeUnit.SECONDS)  // Total timeout of 50 seconds
                .pollInterval(Duration.ofMillis(500))  // Poll every 500 milliseconds
                .untilAsserted(() -> {
                    IngredientMessageDTO receivedMessage = (IngredientMessageDTO) rabbitTemplate.receiveAndConvert(INGREDIENTS_QUEUE);
                    assertNotNull(receivedMessage, "Expected a message in the queue, but none was found.");
                    assertEquals("Salt", receivedMessage.getName());
                    assertEquals(ingredientUuid, receivedMessage.getUuid().toString());
                });
    }

    @Test
    public void testCreateIngredient_MissingName_ShouldReturnBadRequest() throws Exception {
        CreateIngredientRequest request = new CreateIngredientRequest();
        // Name is intentionally left null to test validation

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Ingredient name must not be blank."));
    }

    @Test
    @Transactional
    public void testCreateIngredient_DuplicateName_ShouldReturnConflict() throws Exception {
        log.info("[POSTGRESQL-CONTAINER] PostgreSQL Container started with URL: {}", postgreSQLContainer.getJdbcUrl());

        // Directly insert the ingredient into the database using the repository
        Ingredients existingIngredient = new Ingredients();
        existingIngredient.setUuid(UUID.randomUUID());  // Set the UUID to avoid the not-null constraint violation
        existingIngredient.setName("Salt");
        ingredientRepository.saveAndFlush(existingIngredient);

        // Ensure that the ingredient is in the database
        assertTrue(ingredientRepository.existsByNameIgnoreCase("Salt"));

        // Prepare the request to create the same ingredient again
        CreateIngredientRequest request = new CreateIngredientRequest();
        request.setName("Salt");

        // Try creating the same ingredient again
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ingredient with this name already exists."));
    }

    @Test
    public void testGetIngredientById_Success() throws Exception {
        // Directly insert an ingredient into the database
        final Ingredients ingredient = new Ingredients();
        UUID ingredientUuid = UUID.randomUUID();
        ingredient.setUuid(ingredientUuid);
        ingredient.setName("Pepper");

        ingredientRepository.saveAndFlush(ingredient);

        // Use the UUID of the inserted ingredient to make a GET request
        mockMvc.perform(get(BASE_URL + "/" + ingredientUuid.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(ingredientUuid.toString()))
                .andExpect(jsonPath("$.name").value("Pepper"));

        // Validates the response
        final String responseContent = mockMvc.perform(get(BASE_URL + "/" + ingredientUuid.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Deserialize the response to an IngredientResponse object
        final IngredientResponse response = objectMapper.readValue(responseContent, IngredientResponse.class);

        // Validate the retrieved ingredient matches the inserted ingredient
        assertEquals(ingredientUuid, response.getUuid());
        assertEquals("Pepper", response.getName());
    }

    @Test
    public void testGetIngredientById_NotFound() throws Exception {
        // Generate a random UUID that does not exist in the database
        UUID nonExistentUuid = UUID.randomUUID();

        // Perform the GET request with the non-existent UUID
        mockMvc.perform(get(BASE_URL + "/" + nonExistentUuid.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ingredient Not Found"));
    }

    @Test
    public void testGetAllIngredients_Success() throws Exception {
        // Prepare a list of 10 ingredients to insert into the database
        List<Ingredients> ingredientsList = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> {
                    Ingredients ingredient = new Ingredients();
                    ingredient.setUuid(UUID.randomUUID());
                    ingredient.setName("Ingredient " + i);
                    return ingredient;
                })
                .collect(Collectors.toList());

        // Insert all ingredients at once
        ingredientRepository.saveAllAndFlush(ingredientsList);

        // Use Pageable to request all ingredients
        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.content[0].name").value("Ingredient 10"))  // Based on the default DESC order
                .andExpect(jsonPath("$.content[9].name").value("Ingredient 1"));  // Last item in list
    }


}
