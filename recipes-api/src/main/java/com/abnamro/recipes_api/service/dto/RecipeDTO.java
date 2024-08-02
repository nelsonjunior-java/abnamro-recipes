package com.abnamro.recipes_api.service.dto;

import com.abnamro.recipes_api.controller.request.RecipeRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
public class RecipeDTO implements Serializable {

    private UUID id;
    private String name;
    private Integer servings;
    private String instructions;
    private List<Long> ingredientIds;
    private Boolean vegetarian;

    public static RecipeDTO of(RecipeRequest recipeRequest) {
        RecipeDTO recipeDTO = new RecipeDTO();
        recipeDTO.setName(recipeRequest.getName());
        recipeDTO.setServings(recipeRequest.getServings());
        recipeDTO.setInstructions(recipeRequest.getInstructions());
        recipeDTO.setIngredientIds(recipeRequest.getIngredientIds());
        recipeDTO.setVegetarian(recipeRequest.getIsVegetarian());

        return recipeDTO;
    }
}
