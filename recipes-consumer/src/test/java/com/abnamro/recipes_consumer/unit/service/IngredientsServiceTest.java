package com.abnamro.recipes_consumer.unit.service;

import com.abnamro.recipes_consumer.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_consumer.infra.repository.IngredientRepository;
import com.abnamro.recipes_consumer.model.Ingredients;
import com.abnamro.recipes_consumer.service.IngredientsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class IngredientsServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientsService ingredientsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSave_IngredientIsSaved() {
        // Given
        IngredientMessageDTO ingredientMessageDTO = new IngredientMessageDTO();
        ingredientMessageDTO.setUuid(UUID.randomUUID());
        ingredientMessageDTO.setName("Test Ingredient");

        Ingredients ingredients = Ingredients.of(ingredientMessageDTO);

        // When
        ingredientsService.save(ingredientMessageDTO);

        // Then
        verify(ingredientRepository, times(1)).save(ingredients);
    }

    @Test
    public void testSave_LogMessageIsCorrect() {
        // Given
        IngredientMessageDTO ingredientMessageDTO = new IngredientMessageDTO();
        ingredientMessageDTO.setUuid(UUID.randomUUID());
        ingredientMessageDTO.setName("Test Ingredient");

        // When
        ingredientsService.save(ingredientMessageDTO);

        // Then
        verify(ingredientRepository, times(1)).save(any(Ingredients.class));
    }

}