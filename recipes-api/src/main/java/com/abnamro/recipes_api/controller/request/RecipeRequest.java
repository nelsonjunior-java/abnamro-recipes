package com.abnamro.recipes_api.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Class used on the Recipe api requests
 */
@Data
public class RecipeRequest {

    private String name;

    private Integer servings;

    private Boolean isVegetarian;

    private List<@NotNull(message = "Ingredient ID cannot be null") Long> ingredientIds;

    private String instructions;

}
