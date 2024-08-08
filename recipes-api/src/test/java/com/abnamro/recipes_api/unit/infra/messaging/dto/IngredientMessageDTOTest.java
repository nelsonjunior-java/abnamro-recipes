package com.abnamro.recipes_api.unit.infra.messaging.dto;

import com.abnamro.recipes_api.controller.request.CreateIngredientRequest;
import com.abnamro.recipes_api.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_api.model.Ingredients;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IngredientMessageDTOTest {

    @Test
    public void testOf_CreateIngredientRequest_ShouldMapFieldsCorrectly() {
        // Given
        String ingredientName = "Salt";
        CreateIngredientRequest createIngredientRequest = new CreateIngredientRequest();
        createIngredientRequest.setName(ingredientName);

        // When
        IngredientMessageDTO ingredientMessageDTO = IngredientMessageDTO.of(createIngredientRequest);

        // Then
        assertEquals(ingredientName, ingredientMessageDTO.getName());
        assertNull(ingredientMessageDTO.getId(), "ID should be null for a new ingredient creation request");
        assertNull(ingredientMessageDTO.getUuid(), "UUID should be null for a new ingredient creation request");
    }

    @Test
    public void testOf_Ingredients_ShouldMapFieldsCorrectly() {
        // Given
        Long ingredientId = 1L;
        UUID ingredientUuid = UUID.randomUUID();
        String ingredientName = "Pepper";

        Ingredients ingredientsEntity = new Ingredients();
        ingredientsEntity.setId(ingredientId);
        ingredientsEntity.setUuid(ingredientUuid);
        ingredientsEntity.setName(ingredientName);

        // When
        IngredientMessageDTO ingredientMessageDTO = IngredientMessageDTO.of(ingredientsEntity);

        // Then
        assertEquals(ingredientId, ingredientMessageDTO.getId());
        assertEquals(ingredientUuid, ingredientMessageDTO.getUuid());
        assertEquals(ingredientName, ingredientMessageDTO.getName());
    }

    @Test
    public void testConstructorAndGetters_SetAndGetFieldsCorrectly() {
        // Given
        Long ingredientId = 1L;
        UUID ingredientUuid = UUID.randomUUID();
        String ingredientName = "Sugar";

        // When
        IngredientMessageDTO ingredientMessageDTO = new IngredientMessageDTO();
        ingredientMessageDTO.setId(ingredientId);
        ingredientMessageDTO.setUuid(ingredientUuid);
        ingredientMessageDTO.setName(ingredientName);

        // Then
        assertEquals(ingredientId, ingredientMessageDTO.getId());
        assertEquals(ingredientUuid, ingredientMessageDTO.getUuid());
        assertEquals(ingredientName, ingredientMessageDTO.getName());
    }
}
