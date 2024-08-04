package com.abnamro.recipes_api.controller.reponse;

import com.abnamro.recipes_api.model.Ingredients;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientResponse {

    private UUID uuid;
    private String name;

    public static IngredientResponse of(Ingredients ingredients){

        IngredientResponse ingredientResponse = new IngredientResponse();
        ingredientResponse.setUuid(ingredients.getUuid());
        ingredientResponse.setName(ingredients.getName());

        return ingredientResponse;
    }
}
