package com.abnamro.recipes_api.unit.utils;

import com.abnamro.recipes_api.controller.request.CreateIngredientRequest;
import com.abnamro.recipes_api.model.Ingredients;

import java.util.UUID;

public final class TestUtils {

    private TestUtils() {
        // Prevent instantiation
    }

    public static CreateIngredientRequest createIngredientRequest(final String name) {
        final CreateIngredientRequest request = new CreateIngredientRequest();
        request.setName(name);
        return request;
    }

    public static Ingredients createIngredient(final UUID uuid, final String name) {
        final Ingredients ingredient = new Ingredients();
        ingredient.setUuid(uuid);
        ingredient.setName(name);
        return ingredient;
    }
}
