package com.abnamro.recipes_api.infra.messaging.dto;

import com.abnamro.recipes_api.controller.request.CreateIngredientRequest;
import com.abnamro.recipes_api.infra.messaging.Message;
import lombok.Data;

import java.util.UUID;

@Data
public class IngredientMessageDTO implements Message {

    private UUID uuid;
    private String name;

    public static IngredientMessageDTO of(CreateIngredientRequest createIngredientRequest) {
        IngredientMessageDTO ingredientMessageDTO = new IngredientMessageDTO();
        ingredientMessageDTO.setName(createIngredientRequest.getName());

        return ingredientMessageDTO;
    }


}
