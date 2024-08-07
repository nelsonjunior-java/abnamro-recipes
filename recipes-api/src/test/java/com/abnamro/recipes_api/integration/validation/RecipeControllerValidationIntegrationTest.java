package com.abnamro.recipes_api.integration.validation;

import com.abnamro.recipes_api.controller.request.RecipeRequest;
import com.abnamro.recipes_api.infra.repository.IngredientRepository;
import com.abnamro.recipes_api.integration.BaseIntegrationTest;
import com.abnamro.recipes_api.model.Ingredients;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RecipeControllerValidationIntegrationTest extends BaseIntegrationTest {

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

    private List<String> ingredientIds;

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("abnamro")
            .withUsername("admin")
            .withPassword("admin");

    @BeforeEach
    public void setUp() {
        saveIngredientsToDatabase();
    }

    private static Stream<RecipeRequestTestData> recipeRequestTestData() {
        return Stream.of(
                new RecipeRequestTestData("Name cannot be blank", "", 2, true, "Valid instructions", List.of(), "name"),
                new RecipeRequestTestData("Name cannot be longer than 255 characters", "A".repeat(256), 2, true, "Valid instructions", List.of(), "name"),
                new RecipeRequestTestData("Servings must be a positive number", "Valid Name", 0, true, "Valid instructions", List.of(), "servings"),
                new RecipeRequestTestData("Servings cannot be null", "Valid Name", null, true, "Valid instructions", List.of(), "servings"),
                new RecipeRequestTestData("Vegetarian flag cannot be null", "Valid Name", 2, null, "Valid instructions", List.of(), "isVegetarian"),
                new RecipeRequestTestData("Ingredient IDs cannot be null", "Valid Name", 2, true, "Valid instructions", null, "ingredientIds"),
                new RecipeRequestTestData("Instructions cannot be blank", "Valid Name", 2, true, "", List.of(), "instructions"),
                new RecipeRequestTestData("Instructions cannot be longer than 1000 characters", "Valid Name", 2, true, "B".repeat(1001), List.of(), "instructions")
        );
    }

    @ParameterizedTest
    @MethodSource("recipeRequestTestData")
    public void testRecipeRequestValidation(RecipeRequestTestData testData) throws Exception {
        RecipeRequest request = createRecipeRequest(testData);
        MvcResult result = mockMvc.perform(post("/api/v1/recipe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Validate the error message is related to the specific field
        String content = result.getResponse().getContentAsString();
        assertTrue("Expected error message not found for field: " + testData.getFieldName(), content.contains(testData.getExpectedError()));
    }

    private RecipeRequest createRecipeRequest(RecipeRequestTestData testData) {
        RecipeRequest request = new RecipeRequest();
        request.setName(testData.getName());
        request.setServings(testData.getServings());
        request.setIsVegetarian(testData.getIsVegetarian());
        request.setInstructions(testData.getInstructions());
        request.setIngredientIds(testData.getIngredientIds() != null ? ingredientIds : testData.getIngredientIds());
        return request;
    }

    private void saveIngredientsToDatabase() {
        Ingredients ingredient1 = createIngredient("Tomato");
        Ingredients ingredient2 = createIngredient("Basil");
        Ingredients ingredient3 = createIngredient("Pasta");

        ingredientRepository.saveAll(List.of(ingredient1, ingredient2, ingredient3));

        ingredientIds = List.of(
                ingredient1.getUuid().toString(),
                ingredient2.getUuid().toString(),
                ingredient3.getUuid().toString()
        );
    }

    private Ingredients createIngredient(String name) {
        Ingredients ingredient = new Ingredients();
        ingredient.setUuid(UUID.randomUUID());
        ingredient.setName(name);
        return ingredient;
    }

    private static class RecipeRequestTestData {
        private final String expectedError;
        private final String name;
        private final Integer servings;
        private final Boolean isVegetarian;
        private final String instructions;
        private final List<String> ingredientIds;
        private final String fieldName;

        public RecipeRequestTestData(String expectedError, String name, Integer servings, Boolean isVegetarian, String instructions, List<String> ingredientIds, String fieldName) {
            this.expectedError = expectedError;
            this.name = name;
            this.servings = servings;
            this.isVegetarian = isVegetarian;
            this.instructions = instructions;
            this.ingredientIds = ingredientIds;
            this.fieldName = fieldName;
        }

        public String getExpectedError() {
            return expectedError;
        }

        public String getName() {
            return name;
        }

        public Integer getServings() {
            return servings;
        }

        public Boolean getIsVegetarian() {
            return isVegetarian;
        }

        public String getInstructions() {
            return instructions;
        }

        public List<String> getIngredientIds() {
            return ingredientIds;
        }

        public String getFieldName() {
            return fieldName;
        }
    }
}
