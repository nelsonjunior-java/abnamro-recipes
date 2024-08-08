package com.abnamro.recipes_consumer.unit.infra.messaging.impl;

import com.abnamro.recipes_consumer.infra.messaging.dto.RecipeMessageDTO;
import com.abnamro.recipes_consumer.infra.messaging.impl.RecipeMessageConsumer;
import com.abnamro.recipes_consumer.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;

class RecipeMessageConsumerTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RecipeMessageConsumer recipeMessageConsumer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConsumeMessage() {
        // Given
        RecipeMessageDTO recipeMessageDTO = new RecipeMessageDTO();
        recipeMessageDTO.setName("Test Recipe");
        recipeMessageDTO.setServings(4);
        recipeMessageDTO.setInstructions("Cook for 20 minutes.");
        recipeMessageDTO.setVegetarian(true);

        // When
        recipeMessageConsumer.consumeMessage(recipeMessageDTO);

        // Then
        ArgumentCaptor<RecipeMessageDTO> captor = ArgumentCaptor.forClass(RecipeMessageDTO.class);
        verify(recipeService, times(1)).save(captor.capture());

        RecipeMessageDTO capturedArgument = captor.getValue();
        assertEquals("Test Recipe", capturedArgument.getName());
        assertEquals(4, capturedArgument.getServings());
        assertEquals("Cook for 20 minutes.", capturedArgument.getInstructions());
        assertEquals(true, capturedArgument.getVegetarian());
    }

    @Test
    public void testConsumeMessage_CallsSaveMethod() {
        // Given
        RecipeMessageDTO recipeMessageDTO = new RecipeMessageDTO();
        recipeMessageDTO.setName("Test Recipe");

        // When
        recipeMessageConsumer.consumeMessage(recipeMessageDTO);

        // Then
        verify(recipeService, times(1)).save(recipeMessageDTO);
    }


}