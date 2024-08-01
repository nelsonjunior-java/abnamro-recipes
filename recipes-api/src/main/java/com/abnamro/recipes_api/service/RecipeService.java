package com.abnamro.recipes_api.service;

import com.abnamro.recipes_api.controller.request.RecipeRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class RecipeService {

    public UUID createRecipe(@Valid RecipeRequest recipeDTO) {

//        // Validate that all ingredient IDs exist
//        List<Long> ingredientIds = recipeDTO.getIngredientIds();
//        if (!ingredientService.validateIngredientIds(ingredientIds)) {
//            throw new IllegalArgumentException("One or more ingredient IDs are invalid");
//        }

        log.info("In my service!!!!");

       return null;
    }
}
