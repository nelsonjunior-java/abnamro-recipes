package com.abnamro.recipes_consumer.unit.infra.messaging.impl;

import com.abnamro.recipes_consumer.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_consumer.infra.messaging.impl.IngredientMessageConsumer;
import com.abnamro.recipes_consumer.service.IngredientsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class IngredientMessageConsumerTest {

    @Mock
    private IngredientsService ingredientsService;

    @InjectMocks
    private IngredientMessageConsumer ingredientMessageConsumer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConsumeMessage_CallsSaveMethod() {
        // Given
        IngredientMessageDTO ingredientMessageDTO = new IngredientMessageDTO();
        ingredientMessageDTO.setName("Test Ingredient");

        // When
        ingredientMessageConsumer.consumeMessage(ingredientMessageDTO);

        // Then
        verify(ingredientsService, times(1)).save(ingredientMessageDTO);
    }

    @Test
    public void testLogMessage() {
        // Given
        IngredientMessageDTO ingredientMessageDTO = new IngredientMessageDTO();
        ingredientMessageDTO.setName("Test Ingredient");

        // When
        ingredientMessageConsumer.consumeMessage(ingredientMessageDTO);

        // Then
        verify(ingredientsService, times(1)).save(ingredientMessageDTO);
    }

}