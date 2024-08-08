package com.abnamro.recipes_consumer.unit.service;

import com.abnamro.recipes_consumer.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_consumer.infra.messaging.dto.RecipeMessageDTO;
import com.abnamro.recipes_consumer.infra.repository.IngredientRepository;
import com.abnamro.recipes_consumer.infra.repository.RecipeRepository;
import com.abnamro.recipes_consumer.model.Ingredients;
import com.abnamro.recipes_consumer.model.Recipes;
import com.abnamro.recipes_consumer.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private RecipeService recipeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSave_RecipeIsSavedSuccessfully() {
        // Given
        final UUID ingredientUuid = UUID.randomUUID();
        IngredientMessageDTO ingredientMessageDTO = new IngredientMessageDTO();
        ingredientMessageDTO.setUuid(ingredientUuid);
        ingredientMessageDTO.setName("Salt");

        final RecipeMessageDTO recipeMessageDTO = new RecipeMessageDTO();
        recipeMessageDTO.setId(UUID.randomUUID());
        recipeMessageDTO.setName("Test Recipe");
        recipeMessageDTO.setServings(4);
        recipeMessageDTO.setInstructions("Cook for 20 minutes.");
        recipeMessageDTO.setVegetarian(true);
        recipeMessageDTO.setIngredientIds(List.of(ingredientMessageDTO));

        Ingredients ingredients = new Ingredients();
        ingredients.setUuid(ingredientUuid);
        ingredients.setName("Salt");

        when(ingredientRepository.findByUuidIn(List.of(ingredientUuid))).thenReturn(List.of(ingredients));

        // When
        recipeService.save(recipeMessageDTO);

        // Then
        verify(recipeRepository, times(1)).save(any(Recipes.class));
    }

    @Test
    public void testFindByUuids_ReturnsIngredients() {
        // Given
        UUID ingredientUuid = UUID.randomUUID();
        Ingredients ingredients = new Ingredients();
        ingredients.setUuid(ingredientUuid);
        ingredients.setName("Salt");

        when(ingredientRepository.findByUuidIn(List.of(ingredientUuid))).thenReturn(List.of(ingredients));

        // When
        List<Ingredients> result = recipeService.findByUuids(List.of(ingredientUuid));

        // Then
        verify(ingredientRepository, times(1)).findByUuidIn(List.of(ingredientUuid));
        assertEquals(1, result.size());
        assertEquals(ingredientUuid, result.get(0).getUuid());
    }


}