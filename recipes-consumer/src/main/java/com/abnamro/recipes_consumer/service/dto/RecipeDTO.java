package com.abnamro.recipes_consumer.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
public class RecipeDTO implements Serializable {

    private UUID id;
    private String name;
    private Integer servings;
    private String instructions;
    private List<Long> ingredientIds;
    private Boolean vegetarian;
}
