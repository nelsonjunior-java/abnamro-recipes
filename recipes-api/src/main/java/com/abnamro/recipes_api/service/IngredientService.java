package com.abnamro.recipes_api.service;

import com.abnamro.recipes_api.config.RabbitMQConfig;
import com.abnamro.recipes_api.controller.request.CreateIngredientRequest;
import com.abnamro.recipes_api.infra.messaging.MessageSender;
import com.abnamro.recipes_api.service.dto.IngredientMessageDTO;
import com.abnamro.recipes_api.service.dto.RecipeMessageDTO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Data
@Service
public class IngredientService {

    private String name;

    @Autowired
    private MessageSender messageSender;

    public UUID save(CreateIngredientRequest createIngredientRequest) {

        final UUID uuid = UUID.randomUUID();
        IngredientMessageDTO ingredientMessageDTO = IngredientMessageDTO.of(createIngredientRequest);
        ingredientMessageDTO.setId(UUID.randomUUID());

        messageSender.sendMessage(RabbitMQConfig.INGREDIENT_QUEUE_NAME, ingredientMessageDTO);

        return uuid;
    }
}
