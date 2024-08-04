package com.abnamro.recipes_api.controller.reponse;

import com.abnamro.recipes_api.model.Recipes;
import lombok.Data;

import java.util.UUID;
import java.util.Set;

@Data
public class RecipeResponse {

    private UUID uuid;
    private String name;
    private boolean isVegetarian;
    private Integer servings;
    private String instructions;
    private Set<String> ingredients;

    public static RecipeResponse of(final Recipes recipes) {

        final RecipeResponse recipeResponse = new RecipeResponse();
        recipeResponse.setUuid(recipes.getUuid());
        recipeResponse.setName(recipes.getName());
        recipeResponse.setVegetarian(recipes.isVegetarian());
        recipeResponse.setServings(recipes.getServings());
        recipeResponse.setInstructions(recipes.getInstructions());

        //recipeResponse.setIngredients(rec);

        return recipeResponse;
    }
}
