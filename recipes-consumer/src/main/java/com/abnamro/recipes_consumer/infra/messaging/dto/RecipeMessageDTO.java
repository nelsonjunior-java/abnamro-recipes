package com.abnamro.recipes_consumer.infra.messaging.dto;

import com.abnamro.recipes_consumer.infra.messaging.Message;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RecipeMessageDTO implements Message {

    private UUID id;
    private String name;
    private Integer servings;
    private String instructions;
    private List<IngredientMessageDTO> ingredientIds;
    private Boolean vegetarian;
}
