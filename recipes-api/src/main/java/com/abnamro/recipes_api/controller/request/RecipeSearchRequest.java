package com.abnamro.recipes_api.controller.request;

import lombok.Data;

@Data
public class RecipeSearchRequest {

    private Boolean isVegetarian;
    private Integer servings;
    private String includeIngredient;
    private String excludeIngredient;
    private String instructionText;
}
