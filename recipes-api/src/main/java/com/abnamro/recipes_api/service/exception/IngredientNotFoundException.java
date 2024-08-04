package com.abnamro.recipes_api.service.exception;

public class IngredientNotFoundException extends RuntimeException {

    public IngredientNotFoundException(String message) {
        super(message);
    }
}
