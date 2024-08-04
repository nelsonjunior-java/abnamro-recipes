package com.abnamro.recipes_api.infra.messaging.dto;

import com.abnamro.recipes_api.controller.request.RecipeRequest;
import com.abnamro.recipes_api.infra.messaging.Message;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class RecipeMessageDTO implements Message {

    private UUID id;
    private String name;
    private Integer servings;
    private String instructions;
    private List<IngredientMessageDTO> ingredientIds;
    private Boolean vegetarian;

    public static RecipeMessageDTO of(RecipeRequest recipeRequest, List<IngredientMessageDTO> ingredientMessageDTOs) {
        RecipeMessageDTO recipeMessageDTO = new RecipeMessageDTO();
        recipeMessageDTO.setName(recipeRequest.getName());
        recipeMessageDTO.setServings(recipeRequest.getServings());
        recipeMessageDTO.setInstructions(recipeRequest.getInstructions());
        recipeMessageDTO.setIngredientIds(ingredientMessageDTOs);
        recipeMessageDTO.setVegetarian(recipeRequest.getIsVegetarian());

        return recipeMessageDTO;
    }
}
