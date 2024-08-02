package com.abnamro.recipes_api.service;

import com.abnamro.recipes_api.config.RabbitMQConfig;
import com.abnamro.recipes_api.controller.request.RecipeRequest;
import com.abnamro.recipes_api.service.dto.RecipeDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class RecipeService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    public UUID createRecipe(@Valid RecipeRequest recipeRequest) {

//        // Validate that all ingredient IDs exist
//        List<Long> ingredientIds = recipeDTO.getIngredientIds();
//        if (!ingredientService.validateIngredientIds(ingredientIds)) {
//            throw new IllegalArgumentException("One or more ingredient IDs are invalid");
//        }

        final UUID uuid = UUID.randomUUID();
        RecipeDTO recipeDTO = RecipeDTO.of(recipeRequest);
        recipeDTO.setId(UUID.randomUUID());

        log.info("Sending request to the queue...");
        // Send the recipeDTO to the RabbitMQ exchange with routing key
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.RECIPE_QUEUE_NAME,
                recipeDTO
        );

       return uuid;
    }
}
