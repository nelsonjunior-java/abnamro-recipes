package com.abnamro.recipes_api.integration.util;

import com.abnamro.recipes_api.model.Ingredients;
import com.abnamro.recipes_api.model.Recipes;

import java.util.Set;
import java.util.UUID;

public class TestUtils {

    public static Recipes createVegetarianRecipe(String name, int servings, String instructions) {
        Recipes recipe = new Recipes();
        recipe.setUuid(UUID.randomUUID());
        recipe.setName(name);
        recipe.setVegetarian(true);
        recipe.setServings(servings);
        recipe.setInstructions(instructions);
        return recipe;
    }

    public static Recipes createNonVegetarianRecipe(String name, int servings, String instructions) {
        Recipes recipe = new Recipes();
        recipe.setUuid(UUID.randomUUID());
        recipe.setName(name);
        recipe.setVegetarian(false);
        recipe.setServings(servings);
        recipe.setInstructions(instructions);
        return recipe;
    }

    public static Ingredients createIngredient(String name) {
        Ingredients ingredient = new Ingredients();
        ingredient.setUuid(UUID.randomUUID());
        ingredient.setName(name);
        return ingredient;
    }

    public static Recipes createRecipeWithIngredients(String name, boolean isVegetarian, int servings, String instructions, Set<Ingredients> ingredients) {
        Recipes recipe = new Recipes();
        recipe.setUuid(UUID.randomUUID());
        recipe.setName(name);
        recipe.setVegetarian(isVegetarian);
        recipe.setServings(servings);
        recipe.setInstructions(instructions);
        recipe.setIngredients(ingredients);
        return recipe;
    }

}
