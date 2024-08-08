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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/recipe")
@Validated
@Slf4j
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(@Valid @RequestBody RecipeRequest recipeRequest) {

        log.info("method=createRecipe, request={}", recipeRequest);

        final UUID recipeUuid = recipeService.createRecipe(recipeRequest);

        // Create a response object with the UUID
        final RecipeResponse response = new RecipeResponse();
        response.setUuid(recipeUuid);
        response.setName(recipeRequest.getName());

        return ResponseEntity.created(URI.create("/api/v1/recipe/" + recipeUuid)).body(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search recipes based on various criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recipes", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<RecipeResponse>> searchRecipes(
            @RequestParam(required = false) Boolean isVegetarian,
            @RequestParam(required = false) @Min(1) Integer servings,
            @RequestParam(required = false) @Size(max = 255) String includeIngredient,
            @RequestParam(required = false) @Size(max = 255) String excludeIngredient,
            @RequestParam(required = false) @Size(max = 1000) String instructionText) {  // You can adjust the max size as needed

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

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Delete a recipe by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the recipe"),
            @ApiResponse(responseCode = "404", description = "Recipe not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteRecipe(@PathVariable UUID uuid) {

        log.info("method=deleteRecipe, uuid={}", uuid);

        recipeService.deleteRecipe(uuid);
        return ResponseEntity.noContent().build();
    }


}
