package com.abnamro.recipes_api.controller.request;

import com.abnamro.recipes_api.controller.validator.ValidUUIDList;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * Class used on the Recipe api requests
 */
@Data
public class RecipeRequest {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Servings cannot be null")
    @Positive(message = "Servings must be a positive number")
    private Integer servings;

    @NotNull(message = "Vegetarian flag cannot be null")
    private Boolean isVegetarian;

    @NotNull(message = "Ingredient IDs cannot be null")
    @ValidUUIDList(message = "One or more ingredient IDs are not valid UUIDs")
    private List<String> ingredientIds;

    @NotBlank(message = "Instructions cannot be blank")
    private String instructions;

}
