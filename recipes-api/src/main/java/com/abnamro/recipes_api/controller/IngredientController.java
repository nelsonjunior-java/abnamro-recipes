package com.abnamro.recipes_api.controller;

import com.abnamro.recipes_api.controller.reponse.IngredientResponse;
import com.abnamro.recipes_api.controller.request.CreateIngredientRequest;
import com.abnamro.recipes_api.service.IngredientService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<IngredientResponse> createIngredient(@Valid @RequestBody CreateIngredientRequest createIngredientRequest) {

        log.info("method=createIngredient, request={}", createIngredientRequest);

        UUID uuid = ingredientService.save(createIngredientRequest);

        // Create a response object with the UUID
        IngredientResponse response = new IngredientResponse(uuid);

        return ResponseEntity.ok(response);


    }
}
