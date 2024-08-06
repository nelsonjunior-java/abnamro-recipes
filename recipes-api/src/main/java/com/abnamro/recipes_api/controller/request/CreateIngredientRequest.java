package com.abnamro.recipes_api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateIngredientRequest {

    @NotBlank(message = "Ingredient name must not be blank.")
    @Size(max = 255, message = "Ingredient name must not exceed 255 characters.")
    private String name;
}
