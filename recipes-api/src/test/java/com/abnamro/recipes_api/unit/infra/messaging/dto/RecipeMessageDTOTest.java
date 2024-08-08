package com.abnamro.recipes_api.unit.infra.messaging.dto;

import com.abnamro.recipes_api.controller.request.RecipeRequest;
import com.abnamro.recipes_api.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_api.infra.messaging.dto.RecipeMessageDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RecipeMessageDTOTest {

    @Test
    public void testOf_RecipeRequest_ShouldMapFieldsCorrectly() {
        // Arrange
        String recipeName = "Spaghetti Bolognese";
        Integer servings = 4;
        String instructions = "Cook pasta, add sauce.";
        Boolean isVegetarian = false;

        RecipeRequest recipeRequest = new RecipeRequest();
        recipeRequest.setName(recipeName);
        recipeRequest.setServings(servings);
        recipeRequest.setInstructions(instructions);
        recipeRequest.setIsVegetarian(isVegetarian);

        IngredientMessageDTO ingredient1 = new IngredientMessageDTO();
        ingredient1.setId(1L);
        ingredient1.setName("Tomato");

        IngredientMessageDTO ingredient2 = new IngredientMessageDTO();
        ingredient2.setId(2L);
        ingredient2.setName("Ground Beef");

        List<IngredientMessageDTO> ingredientMessageDTOs = List.of(ingredient1, ingredient2);

        // Act
        RecipeMessageDTO recipeMessageDTO = RecipeMessageDTO.of(recipeRequest, ingredientMessageDTOs);

        // Assert
        assertNull(recipeMessageDTO.getId(), "ID should be null as it's not set by RecipeRequest");
        assertEquals(recipeName, recipeMessageDTO.getName());
        assertEquals(servings, recipeMessageDTO.getServings());
        assertEquals(instructions, recipeMessageDTO.getInstructions());
        assertEquals(isVegetarian, recipeMessageDTO.getVegetarian());
        assertEquals(ingredientMessageDTOs, recipeMessageDTO.getIngredientIds());
    }

    @Test
    public void testConstructorAndGetters_SetAndGetFieldsCorrectly() {
        // Arrange
        UUID recipeId = UUID.randomUUID();
        String recipeName = "Vegetarian Pizza";
        Integer servings = 2;
        String instructions = "Prepare dough, add toppings, bake.";
        Boolean isVegetarian = true;

        IngredientMessageDTO ingredient1 = new IngredientMessageDTO();
        ingredient1.setId(3L);
        ingredient1.setName("Cheese");

        IngredientMessageDTO ingredient2 = new IngredientMessageDTO();
        ingredient2.setId(4L);
        ingredient2.setName("Tomato Sauce");

        List<IngredientMessageDTO> ingredientMessageDTOs = List.of(ingredient1, ingredient2);

        // Act
        RecipeMessageDTO recipeMessageDTO = new RecipeMessageDTO();
        recipeMessageDTO.setId(recipeId);
        recipeMessageDTO.setName(recipeName);
        recipeMessageDTO.setServings(servings);
        recipeMessageDTO.setInstructions(instructions);
        recipeMessageDTO.setIngredientIds(ingredientMessageDTOs);
        recipeMessageDTO.setVegetarian(isVegetarian);

        // Assert
        assertEquals(recipeId, recipeMessageDTO.getId());
        assertEquals(recipeName, recipeMessageDTO.getName());
        assertEquals(servings, recipeMessageDTO.getServings());
        assertEquals(instructions, recipeMessageDTO.getInstructions());
        assertEquals(isVegetarian, recipeMessageDTO.getVegetarian());
        assertEquals(ingredientMessageDTOs, recipeMessageDTO.getIngredientIds());
    }
}
