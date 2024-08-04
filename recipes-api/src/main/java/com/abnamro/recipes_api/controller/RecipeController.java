package com.abnamro.recipes_api.controller;

import com.abnamro.recipes_api.controller.reponse.RecipeResponse;
import com.abnamro.recipes_api.controller.request.RecipeRequest;
import com.abnamro.recipes_api.controller.request.RecipeSearchRequest;
import com.abnamro.recipes_api.model.Recipes;
import com.abnamro.recipes_api.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

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

        return ResponseEntity.ok("Recipe creation request successfully received!");
    }

    @GetMapping("/search")
    @Operation(summary = "Search recipes based on various criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recipes", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<RecipeResponse>> searchRecipes(
            @RequestParam(required = false) Boolean isVegetarian,
            @RequestParam(required = false) Integer servings,
            @RequestParam(required = false) String includeIngredient,
            @RequestParam(required = false) String excludeIngredient,
            @RequestParam(required = false) String instructionText) {

        log.info("method=searchRecipes, isVegetarian={}, servings={}, includeIngredient={}, excludeIngredient={}, instructionText={}",
                isVegetarian, servings, includeIngredient, excludeIngredient, instructionText);

        RecipeSearchRequest searchRequest = new RecipeSearchRequest();
        searchRequest.setIsVegetarian(isVegetarian);
        searchRequest.setServings(servings);
        searchRequest.setIncludeIngredient(includeIngredient);
        searchRequest.setExcludeIngredient(excludeIngredient);
        searchRequest.setInstructionText(instructionText);

        final List<Recipes> recipes = recipeService.searchRecipes(searchRequest);
        final List<RecipeResponse> recipesResponseList = recipes.stream().map(RecipeResponse::of).toList();

        return ResponseEntity.ok(recipesResponseList);
    }

}
