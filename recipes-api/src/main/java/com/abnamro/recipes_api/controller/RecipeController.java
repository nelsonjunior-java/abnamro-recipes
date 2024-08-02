package com.abnamro.recipes_api.controller;

import com.abnamro.recipes_api.controller.request.RecipeRequest;
import com.abnamro.recipes_api.service.RecipeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/recipe")
@Slf4j
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public ResponseEntity<String> createRecipe(@Valid @RequestBody RecipeRequest recipe) {

        log.info("method=createRecipe, request={}", recipe);

        recipeService.createRecipe(recipe);

        return ResponseEntity.ok("Recipe created and added to queue successfully");
    }

}
