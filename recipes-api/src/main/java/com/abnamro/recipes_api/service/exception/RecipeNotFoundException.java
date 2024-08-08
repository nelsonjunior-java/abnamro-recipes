package com.abnamro.recipes_api.service.exception;

public class RecipeNotFoundException extends RuntimeException {

    public RecipeNotFoundException(String message) {
        super(message);
    }
}
