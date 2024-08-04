package com.abnamro.recipes_api.controller;

import com.abnamro.recipes_api.controller.reponse.IngredientResponse;
import com.abnamro.recipes_api.controller.request.CreateIngredientRequest;
import com.abnamro.recipes_api.model.Ingredients;
import com.abnamro.recipes_api.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/ingredient")
@Slf4j
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @PostMapping
    @Operation(description = "Creates an Ingredient")
    public ResponseEntity<IngredientResponse> createIngredient(@Valid @RequestBody CreateIngredientRequest createIngredientRequest) {

        log.info("method=createIngredient, request={}", createIngredientRequest);

        UUID uuid = ingredientService.save(createIngredientRequest);

        // Create a response object with the UUID
        IngredientResponse response = new IngredientResponse();
        response.setUuid(uuid);

        return ResponseEntity.ok(response);

    }

    @Operation(description = "Retrieves an ingredient by its UUID")
    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    public IngredientResponse getIngredientById(@Parameter(description = "Ingredient UUID", required = true)
                                                    @PathVariable(name = "uuid") String uuid) {
        log.info("method=getIngredientById, request uuid={}", uuid);

        Ingredients ingredients = ingredientService.findByUuid(uuid);

        return IngredientResponse.of(ingredients);
    }

    @Operation(description = "List all ingredients")
    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ingredients", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public Page<IngredientResponse> getAllIngredients(
            @Parameter(description = "Pagination and Sorting information")
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        final Page<Ingredients> ingredientsPage = ingredientService.findAll(pageable);

        return ingredientsPage.map(IngredientResponse::of);
    }

    @Operation(description = "Delete a ingredient by its UUID")
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteRecipe(
            @Parameter(description = "Ingredient ID", required = true)
            @NotNull(message = "{id.notNull}")
            @RequestParam(name = "uuid") String uuid) {

        log.info("method=deleteRecipe, request uuid={}", uuid);

        final boolean isDeleted = ingredientService.delete(uuid);

        if (isDeleted) {
            return ResponseEntity.ok("The Ingredient was successfully deleted");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ingredient not found");
        }
    }
}
