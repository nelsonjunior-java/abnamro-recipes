package com.abnamro.recipes_api.integration;

import com.abnamro.recipes_api.controller.reponse.RecipeResponse;
import com.abnamro.recipes_api.controller.request.RecipeRequest;
import com.abnamro.recipes_api.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_api.infra.messaging.dto.RecipeMessageDTO;
import com.abnamro.recipes_api.infra.repository.IngredientRepository;
import com.abnamro.recipes_api.infra.repository.RecipeRepository;
import com.abnamro.recipes_api.integration.util.TestUtils;
import com.abnamro.recipes_api.model.Ingredients;
import com.abnamro.recipes_api.model.Recipes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@Slf4j
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RecipeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String BASE_URL = "/api/v1/recipe";
    private static final String RECIPES_QUEUE = "recipes_queue";
    private static final String PASTA = "Pasta";
    private static final String TOMATO = "Tomato";
    private static final String BASIL = "Basil";
    private static final String INSTRUCTIONS = "Boil pasta. Make sauce. Combine.";
    private static final boolean IS_VEGETARIAN = true;
    private static final int SERVINGS = 2;
    private static final long AWAIT_TIMEOUT = 60;
    private static final long POLL_INTERVAL = 500;

    private List<String> ingredientIds;

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("abnamro")
            .withUsername("admin")
            .withPassword("admin");

    @Container
    private static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management");


    @BeforeEach
    public void setUp() {
        ingredientRepository.deleteAll();
        saveIngredientsToDatabase();
    }

    @Test
    public void testCreateRecipe_Success() throws Exception {
        final RecipeRequest request = createRecipeRequest(PASTA, IS_VEGETARIAN, SERVINGS, ingredientIds, INSTRUCTIONS);
        final RecipeResponse response = sendCreateRecipeRequestAndGetResponse(request);
        validateRecipeResponse(response, request);
    }

    @Test
    public void testCreateRecipeAndValidateQueueMessage() throws Exception {
        final RecipeRequest request = createRecipeRequest(PASTA, IS_VEGETARIAN, SERVINGS, ingredientIds, INSTRUCTIONS);
        final RecipeResponse response = sendCreateRecipeRequestAndGetResponse(request);

        validateRecipeResponse(response, request);
        validateQueueMessage(response, request);
    }

    @Test
    public void testCreateRecipeWithNonExistentIngredientIds_ShouldReturnNotFound() throws Exception {
        // Create a RecipeRequest with non-existent ingredient UUIDs
        final List<String> nonExistentIngredientIds = List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        final RecipeRequest request = new RecipeRequest();
        request.setName("Pasta");
        request.setIsVegetarian(true);
        request.setServings(2);
        request.setIngredientIds(nonExistentIngredientIds);  // This list contains non-existent ingredient UUIDs
        request.setInstructions("Boil pasta. Make sauce. Combine.");

        // Send POST request to create recipe and expect a 404 Not Found status
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ingredient Not Found"))
                .andExpect(jsonPath("$.message").value("Ingredient with UUID " + nonExistentIngredientIds.get(0) + " does not exist"));
    }


    @Test
    public void testSearchRecipes_ReturnAllVegetarianRecipes() throws Exception {
        // Insert ingredients into the database
        final Ingredients tomato = TestUtils.createIngredient("Tomato");
        final Ingredients basil = TestUtils.createIngredient("Basil");

        ingredientRepository.saveAll(List.of(tomato, basil));

        // Insert vegetarian recipes into the database
        Recipes vegetarianRecipe1 = TestUtils.createRecipeWithIngredients("Vegetarian Salad", true, 2, "Mix all ingredients.", Set.of(tomato));
        Recipes vegetarianRecipe2 = TestUtils.createRecipeWithIngredients("Vegetarian Stir Fry", true, 4, "Stir fry vegetables.", Set.of(basil));

        recipeRepository.saveAll(List.of(vegetarianRecipe1, vegetarianRecipe2));

        // Insert a non-vegetarian recipe into the database for negative control
        Recipes nonVegetarianRecipe = TestUtils.createRecipeWithIngredients("Chicken Curry", false, 4, "Cook chicken with curry spices.", Set.of());

        recipeRepository.save(nonVegetarianRecipe);

        // Perform GET request with the isVegetarian=true filter
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("isVegetarian", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2)) // Expect 2 recipes
                .andExpect(jsonPath("$[0].name").value("Vegetarian Salad"))
                .andExpect(jsonPath("$[1].name").value("Vegetarian Stir Fry"))
                .andExpect(jsonPath("$[0].vegetarian").value(true))
                .andExpect(jsonPath("$[1].vegetarian").value(true))
                .andExpect(jsonPath("$[0].uuid").exists())
                .andExpect(jsonPath("$[1].uuid").exists());
    }

    @Test
    public void testSearchRecipes_ReturnAllNonVegetarianRecipes() throws Exception {
        // Insert ingredients into the database
        final Ingredients chicken = TestUtils.createIngredient("Chicken");
        final Ingredients beef = TestUtils.createIngredient("Beef");

        ingredientRepository.saveAll(List.of(chicken, beef));

        // Insert vegetarian recipes into the database
        Recipes vegetarianRecipe1 = TestUtils.createRecipeWithIngredients("Vegetarian Salad", true, 2, "Mix all ingredients.", Set.of());
        Recipes vegetarianRecipe2 = TestUtils.createRecipeWithIngredients("Vegetarian Stir Fry", true, 4, "Stir fry vegetables.", Set.of());
        recipeRepository.saveAll(List.of(vegetarianRecipe1, vegetarianRecipe2));

        // Insert non-vegetarian recipes into the database
        Recipes nonVegetarianRecipe1 = TestUtils.createRecipeWithIngredients("Chicken Curry", false, 4, "Cook chicken with curry spices.", Set.of(chicken));
        Recipes nonVegetarianRecipe2 = TestUtils.createRecipeWithIngredients("Beef Stew", false, 6, "Slow cook beef with vegetables.", Set.of(beef));
        recipeRepository.saveAll(List.of(nonVegetarianRecipe1, nonVegetarianRecipe2));

        // Perform GET request with the isVegetarian=false filter
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("isVegetarian", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2)) // Expect 2 recipes
                .andExpect(jsonPath("$[0].name").value("Chicken Curry"))
                .andExpect(jsonPath("$[1].name").value("Beef Stew"))
                .andExpect(jsonPath("$[0].vegetarian").value(false))
                .andExpect(jsonPath("$[1].vegetarian").value(false))
                .andExpect(jsonPath("$[0].uuid").exists())
                .andExpect(jsonPath("$[1].uuid").exists());
    }

    @Test
    public void testSearchRecipes_FilterByServings() throws Exception {
        // Insert ingredients into the database
        final Ingredients tomato = TestUtils.createIngredient("Tomato");
        final Ingredients chicken = TestUtils.createIngredient("Chicken");

        ingredientRepository.saveAll(List.of(tomato, chicken));

        // Insert recipes with different servings into the database
        final Recipes recipeWithTwoServings = TestUtils.createRecipeWithIngredients("Vegetarian Salad", true, 2, "Mix all ingredients.", Set.of(tomato));
        final Recipes recipeWithFourServings = TestUtils.createRecipeWithIngredients("Chicken Curry", false, 4, "Cook chicken with curry spices.", Set.of(chicken));
        final Recipes recipeWithSixServings = TestUtils.createRecipeWithIngredients("Vegetarian Pasta", true, 6, "Boil pasta. Mix with sauce.", Set.of(tomato));

        recipeRepository.saveAll(List.of(recipeWithTwoServings, recipeWithFourServings, recipeWithSixServings));

        // Perform GET request with the servings=4 filter
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("servings", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1)) // Expect 1 recipe with 4 servings
                .andExpect(jsonPath("$[0].name").value("Chicken Curry"))
                .andExpect(jsonPath("$[0].servings").value(4))
                .andExpect(jsonPath("$[0].uuid").exists());
    }

    @Test
    public void testSearchRecipes_InvalidServings_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("servings", "0") // Invalid servings value
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['searchRecipes.servings']").value("must be greater than or equal to 1"));
    }

    @Test
    public void testSearchRecipes_FilterByIncludeIngredient() throws Exception {
        // Insert ingredients into the database
        final Ingredients tomato = TestUtils.createIngredient("Tomato");
        final Ingredients chicken = TestUtils.createIngredient("Chicken");
        final Ingredients basil = TestUtils.createIngredient("Basil");

        ingredientRepository.saveAll(List.of(tomato, chicken, basil));

        // Insert recipes with and without the specified ingredient into the database
        final Recipes recipeWithTomato = TestUtils.createRecipeWithIngredients("Tomato Pasta", true, 2, "Cook pasta with tomato sauce.", Set.of(tomato));
        final Recipes recipeWithChicken = TestUtils.createRecipeWithIngredients("Chicken Curry", false, 4, "Cook chicken with curry spices.", Set.of(chicken));
        final Recipes recipeWithTomatoAndBasil = TestUtils.createRecipeWithIngredients("Tomato Basil Salad", true, 1, "Mix tomato and basil.", Set.of(tomato, basil));

        recipeRepository.saveAll(List.of(recipeWithTomato, recipeWithChicken, recipeWithTomatoAndBasil));

        // Perform GET request to search recipes including "Tomato" as an ingredient
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("includeIngredient", "Tomato")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2)) // Expect 2 recipes with "Tomato"
                .andExpect(jsonPath("$[0].name").value("Tomato Basil Salad"))
                .andExpect(jsonPath("$[1].name").value("Tomato Pasta"))
                .andExpect(jsonPath("$[0].ingredients[0]").value("Basil"))
                .andExpect(jsonPath("$[0].ingredients[1]").value("Tomato"))
                .andExpect(jsonPath("$[1].ingredients[0]").value("Tomato"));
    }

    @Test
    public void testSearchRecipes_FilterByIncludeIngredient_NoResults() throws Exception {
        // Insert ingredients into the database
        final Ingredients tomato = TestUtils.createIngredient("Tomato");
        final Ingredients chicken = TestUtils.createIngredient("Chicken");
        final Ingredients basil = TestUtils.createIngredient("Basil");

        ingredientRepository.saveAll(List.of(tomato, chicken, basil));

        // Insert recipes that do not contain the specific ingredient ("Potato" in this case)
        final Recipes recipeWithTomato = TestUtils.createRecipeWithIngredients("Tomato Pasta", true, 2, "Cook pasta with tomato sauce.", Set.of(tomato));
        final Recipes recipeWithChicken = TestUtils.createRecipeWithIngredients("Chicken Curry", false, 4, "Cook chicken with curry spices.", Set.of(chicken));
        final Recipes recipeWithTomatoAndBasil = TestUtils.createRecipeWithIngredients("Tomato Basil Salad", true, 1, "Mix tomato and basil.", Set.of(tomato, basil));

        recipeRepository.saveAll(List.of(recipeWithTomato, recipeWithChicken, recipeWithTomatoAndBasil));

        // Perform GET request to search recipes including an ingredient that does not exist in any recipe
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("includeIngredient", "Potato") // "Potato" is not in any of the recipes
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0)); // Expect no recipes to be returned
    }

    @Test
    public void testSearchRecipes_FilterByExcludeIngredient() throws Exception {
        // Step 1: Insert ingredients into the database
        final Ingredients tomato = TestUtils.createIngredient("Tomato");
        final Ingredients chicken = TestUtils.createIngredient("Chicken");
        final Ingredients basil = TestUtils.createIngredient("Basil");

        ingredientRepository.saveAll(List.of(tomato, chicken, basil));

        // Insert recipes with various ingredients
        final Recipes recipeWithTomato = TestUtils.createRecipeWithIngredients("Tomato Pasta", true, 2, "Cook pasta with tomato sauce.", Set.of(tomato));
        final Recipes recipeWithChicken = TestUtils.createRecipeWithIngredients("Chicken Curry", false, 4, "Cook chicken with curry spices.", Set.of(chicken));
        final Recipes recipeWithTomatoAndBasil = TestUtils.createRecipeWithIngredients("Tomato Basil Salad", true, 1, "Mix tomato and basil.", Set.of(tomato, basil));

        recipeRepository.saveAll(List.of(recipeWithTomato, recipeWithChicken, recipeWithTomatoAndBasil));

        // Perform GET request to search recipes excluding "Tomato"
        MvcResult mvcResult = mockMvc.perform(get(BASE_URL + "/search")
                        .param("excludeIngredient", "Tomato")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1)) // Expect only 1 recipe to be returned
                .andExpect(jsonPath("$[0].name").value("Chicken Curry")) // This recipe should not include "Tomato"
                .andReturn();

        // Deserialize the response content to ensure "Tomato" is excluded
        String responseContent = mvcResult.getResponse().getContentAsString();
        RecipeResponse[] responses = objectMapper.readValue(responseContent, RecipeResponse[].class);

        for (RecipeResponse recipe : responses) {
            assertFalse(recipe.getIngredients().contains("Tomato"), "The recipe contains 'Tomato' when it should be excluded.");
        }
    }

    @Test
    public void testSearchRecipes_FilterByExcludeIngredient_NoResults() throws Exception {
        // Insert ingredients into the database
        final Ingredients tomato = TestUtils.createIngredient("Tomato");
        final Ingredients chicken = TestUtils.createIngredient("Chicken");
        final Ingredients basil = TestUtils.createIngredient("Basil");

        ingredientRepository.saveAll(List.of(tomato, chicken, basil));

        // Insert recipes that all include the ingredient to be excluded
        final Recipes recipeWithTomato = TestUtils.createRecipeWithIngredients("Tomato Pasta", true, 2, "Cook pasta with tomato sauce.", Set.of(tomato));
        final Recipes recipeWithTomatoAndBasil = TestUtils.createRecipeWithIngredients("Tomato Basil Salad", true, 1, "Mix tomato and basil.", Set.of(tomato, basil));

        recipeRepository.saveAll(List.of(recipeWithTomato, recipeWithTomatoAndBasil));

        // Perform GET request to search recipes excluding "Tomato"
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("excludeIngredient", "Tomato")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0)); // Expect no recipes to be returned
    }

    @Test
    public void testSearchRecipes_NoCriteria_ReturnAllRecipes() throws Exception {
        // Insert ingredients into the database
        final Ingredients tomato = TestUtils.createIngredient("Tomato");
        final Ingredients chicken = TestUtils.createIngredient("Chicken");
        final Ingredients basil = TestUtils.createIngredient("Basil");

        ingredientRepository.saveAll(List.of(tomato, chicken, basil));

        // Insert various recipes into the database
        final Recipes recipeWithTomato = TestUtils.createRecipeWithIngredients("Tomato Pasta", true, 2, "Cook pasta with tomato sauce.", Set.of(tomato));
        final Recipes recipeWithChicken = TestUtils.createRecipeWithIngredients("Chicken Curry", false, 4, "Cook chicken with curry spices.", Set.of(chicken));
        final Recipes recipeWithTomatoAndBasil = TestUtils.createRecipeWithIngredients("Tomato Basil Salad", true, 1, "Mix tomato and basil.", Set.of(tomato, basil));

        recipeRepository.saveAll(List.of(recipeWithTomato, recipeWithChicken, recipeWithTomatoAndBasil));

        // Perform GET request with no filter criteria
        mockMvc.perform(get(BASE_URL + "/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3)) // Expect all 3 recipes to be returned
                .andExpect(jsonPath("$[?(@.name == 'Tomato Pasta')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Chicken Curry')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Tomato Basil Salad')]").exists());
    }

    @Test
    public void testSearchRecipes_AllFilters_Selected() throws Exception {
        // Insert ingredients into the database
        final Ingredients tomato = TestUtils.createIngredient("Tomato");
        final Ingredients chicken = TestUtils.createIngredient("Chicken");
        final Ingredients basil = TestUtils.createIngredient("Basil");

        ingredientRepository.saveAll(List.of(tomato, chicken, basil));

        // Insert recipes into the database
        final Recipes recipe1 = TestUtils.createRecipeWithIngredients("Tomato Basil Pasta", true, 2, "Cook pasta with tomato and basil.", Set.of(tomato, basil));
        final Recipes recipe2 = TestUtils.createRecipeWithIngredients("Chicken Pasta", false, 4, "Cook pasta with chicken.", Set.of(chicken));
        final Recipes recipe3 = TestUtils.createRecipeWithIngredients("Vegetarian Basil Stir Fry", true, 2, "Stir fry vegetables with basil.", Set.of(basil));

        recipeRepository.saveAll(List.of(recipe1, recipe2, recipe3));

        // Perform GET request with all filters
        MvcResult mvcResult = mockMvc.perform(get(BASE_URL + "/search")
                        .param("isVegetarian", "true")
                        .param("servings", "2")
                        .param("includeIngredient", "Basil")
                        .param("excludeIngredient", "Tomato")
                        .param("instructionText", "Stir fry")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1)) // Expect only 1 recipe to be returned
                .andExpect(jsonPath("$[0].name").value("Vegetarian Basil Stir Fry")) // This recipe matches all the criteria
                .andReturn();

        // Deserialize the response content to ensure "Tomato" is excluded
        String responseContent = mvcResult.getResponse().getContentAsString();
        RecipeResponse[] responses = objectMapper.readValue(responseContent, RecipeResponse[].class);

        // Validate that the returned recipe does not contain "Tomato" in the ingredients
        for (RecipeResponse recipe : responses) {
            assertFalse(recipe.getIngredients().contains("Tomato"), "The recipe contains 'Tomato' when it should be excluded.");
        }
    }

    @Test
    public void testSearchRecipes_EmptyDatabase_ReturnEmptyList() throws Exception {
        // Test goal: Validate that an empty list is returned when there are no recipes in the database

        // Ensure the database is empty (optional, assuming database is reset before each test)
        recipeRepository.deleteAll();
        ingredientRepository.deleteAll();

        // Perform GET request with no filter criteria
        mockMvc.perform(get(BASE_URL + "/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0)); // Expect an empty list since there are no recipes
    }

    @Test
    public void testSearchRecipes_TsVector_PartialMatching() throws Exception {
        // Insert ingredients and recipes into the database
        final Ingredients pasta = TestUtils.createIngredient("Pasta");
        final Ingredients tomato = TestUtils.createIngredient("Tomato");

        ingredientRepository.saveAll(List.of(pasta, tomato));

        final Recipes recipe = TestUtils.createRecipeWithIngredients("Tomato Pasta", true, 2, "Cook pasta with tomato sauce.", Set.of(pasta, tomato));
        recipeRepository.save(recipe);

        // Test for partial matching
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("instructionText", "pasta tomato")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Tomato Pasta"));
    }

    @Test
    public void testSearchRecipes_TsVector_WordOrderVariation() throws Exception {
        // Insert ingredients and recipes into the database
        final Ingredients pasta = TestUtils.createIngredient("Pasta");
        final Ingredients basil = TestUtils.createIngredient("Basil");

        ingredientRepository.saveAll(List.of(pasta, basil));

        final Recipes recipe = TestUtils.createRecipeWithIngredients("Basil Pasta", true, 2, "Boil pasta, add basil.", Set.of(pasta, basil));
        recipeRepository.save(recipe);

        // Test for word order variation
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("instructionText", "add basil boil pasta")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Basil Pasta"));
    }

    @Test
    public void testSearchRecipes_TsVector_WordOrderVariation_LongInstruction() throws Exception {
        // Insert ingredients into the database
        final Ingredients pasta = TestUtils.createIngredient("Pasta");
        final Ingredients tomato = TestUtils.createIngredient("Tomato");
        final Ingredients garlic = TestUtils.createIngredient("Garlic");
        final Ingredients basil = TestUtils.createIngredient("Basil");

        ingredientRepository.saveAll(List.of(pasta, tomato, garlic, basil));

        // Insert a recipe with a longer instruction
        final Recipes recipe = TestUtils.createRecipeWithIngredients(
                "Tomato Basil Pasta",
                true,
                4,
                "Boil the pasta until al dente. In a separate pan, sauté garlic in olive oil, " +
                        "then add fresh tomatoes and cook until softened. Toss the pasta with the tomato " +
                        "mixture, add fresh basil, and serve hot.",
                Set.of(pasta, tomato, garlic, basil)
        );
        recipeRepository.save(recipe);

        // Test for matching with different word order
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("instructionText", "serve hot pasta with basil and garlic")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1)) // Expect 1 matching recipe
                .andExpect(jsonPath("$[0].name").value("Tomato Basil Pasta")); // Validate the correct recipe is returned
    }

    @Test
    public void testDeleteRecipe_Success() throws Exception {
        // Insert ingredients into the database
        final Ingredients pasta = TestUtils.createIngredient("Pasta");
        final Ingredients tomato = TestUtils.createIngredient("Tomato");
        final Ingredients garlic = TestUtils.createIngredient("Garlic");
        final Ingredients basil = TestUtils.createIngredient("Basil");

        ingredientRepository.saveAll(List.of(pasta, tomato, garlic, basil));
        // Arrange: Save a recipe to the database
        final Recipes recipe = TestUtils.createRecipeWithIngredients("Basil Pasta", true, 2, "Boil pasta, add basil.", Set.of(pasta, basil));
        recipeRepository.save(recipe);

        // Act & Assert: Perform the delete operation and validate the response
        mockMvc.perform(delete("/api/v1/recipe/" + recipe.getUuid())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify: Ensure the recipe has been deleted from the database
        assertFalse(recipeRepository.findByUuid(recipe.getUuid()).isPresent());
    }

    @Test
    public void testDeleteRecipe_NotFound() throws Exception {
        // Insert ingredients into the database
        final Ingredients pasta = TestUtils.createIngredient("Pasta");
        final Ingredients tomato = TestUtils.createIngredient("Tomato");
        final Ingredients garlic = TestUtils.createIngredient("Garlic");
        final Ingredients basil = TestUtils.createIngredient("Basil");

        ingredientRepository.saveAll(List.of(pasta, tomato, garlic, basil));

        // Save a recipe to the database
        final Recipes recipe = TestUtils.createRecipeWithIngredients("Tomato Basil Pasta", true, 4, "Cook pasta with tomato and basil.", Set.of(pasta, tomato, basil));
        recipeRepository.save(recipe);

        // Try to delete a non-existent recipe and expect a 404 response
        final UUID nonExistentUuid = UUID.randomUUID(); // Use a UUID that is different from the saved recipe's UUID
        mockMvc.perform(delete("/api/v1/recipe/" + nonExistentUuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Verify: Ensure the original recipe is still present in the database
        assertTrue(recipeRepository.findByUuid(recipe.getUuid()).isPresent());
    }

    @Test
    public void testDeleteRecipe_NonExistentUuid_ShouldReturnNotFound() throws Exception {
        // Attempt to delete a recipe with a UUID that does not exist
        final UUID nonExistentUuid = UUID.randomUUID(); // Generates a random valid UUID that is unlikely to exist in your database

        MvcResult result = mockMvc.perform(delete("/api/v1/recipe/" + nonExistentUuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recipe Not Found"))
                .andReturn();
    }





    private void saveIngredientsToDatabase() {
        final Ingredients ingredient1 = createIngredient(TOMATO);
        final Ingredients ingredient2 = createIngredient(BASIL);
        final Ingredients ingredient3 = createIngredient(PASTA);

        ingredientRepository.saveAll(List.of(ingredient1, ingredient2, ingredient3));

        ingredientIds = List.of(
                ingredient1.getUuid().toString(),
                ingredient2.getUuid().toString(),
                ingredient3.getUuid().toString()
        );
    }

    private Ingredients createIngredient(final String name) {
        final Ingredients ingredient = new Ingredients();
        ingredient.setUuid(UUID.randomUUID());
        ingredient.setName(name);
        return ingredient;
    }

    private RecipeRequest createRecipeRequest(final String name, final boolean isVegetarian, final int servings, final List<String> ingredientIds, final String instructions) {
        final RecipeRequest request = new RecipeRequest();
        request.setName(name);
        request.setIsVegetarian(isVegetarian);
        request.setServings(servings);
        request.setIngredientIds(ingredientIds);
        request.setInstructions(instructions);
        return request;
    }

    private RecipeResponse sendCreateRecipeRequestAndGetResponse(final RecipeRequest request) throws Exception {
        final MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(header().string("Location", startsWith(BASE_URL + "/")))
                .andReturn();

        final String responseContent = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseContent, RecipeResponse.class);
    }

    private void validateRecipeResponse(final RecipeResponse response, final RecipeRequest request) {
        assertNotNull(response.getUuid(), "Expected a valid UUID in the response");
        assertDoesNotThrow(() -> UUID.fromString(response.getUuid().toString()), "Response UUID format is invalid");
        assertEquals(request.getName(), response.getName(), "Recipe name mismatch between request and response");
    }

    private void validateQueueMessage(final RecipeResponse response, final RecipeRequest request) {
        Awaitility.await()
                .atMost(AWAIT_TIMEOUT, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(POLL_INTERVAL))
                .untilAsserted(() -> {
                    final RecipeMessageDTO receivedMessage = (RecipeMessageDTO) rabbitTemplate.receiveAndConvert(RECIPES_QUEUE);
                    assertNotNull(receivedMessage, "Expected a message in the queue, but none was found.");
                    assertEquals(response.getUuid(), receivedMessage.getId(), "UUID mismatch between response and queue message");
                    assertEquals(request.getName(), receivedMessage.getName(), "Name mismatch between request and queue message");
                    assertEquals(request.getIsVegetarian(), receivedMessage.getVegetarian(), "Vegetarian flag mismatch between request and queue message");
                    assertEquals(request.getServings(), receivedMessage.getServings(), "Servings mismatch between request and queue message");
                    assertEquals(request.getInstructions(), receivedMessage.getInstructions(), "Instructions mismatch between request and queue message");

                    final List<String> receivedIngredientIds = extractIngredientIdsFromMessage(receivedMessage);
                    assertEquals(request.getIngredientIds(), receivedIngredientIds, "Ingredient IDs mismatch between request and queue message");
                });
    }

    private List<String> extractIngredientIdsFromMessage(final RecipeMessageDTO receivedMessage) {
        return receivedMessage.getIngredientIds().stream()
                .map(IngredientMessageDTO::getUuid)
                .map(UUID::toString)
                .collect(Collectors.toList());
    }
}
