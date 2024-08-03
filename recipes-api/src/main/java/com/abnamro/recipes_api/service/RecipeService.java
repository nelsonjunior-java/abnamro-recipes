package com.abnamro.recipes_api.service;

import com.abnamro.recipes_api.config.RabbitMQConfig;
import com.abnamro.recipes_api.controller.request.RecipeRequest;
import com.abnamro.recipes_api.infra.messaging.MessageSender;
import com.abnamro.recipes_api.service.dto.RecipeMessageDTO;
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
    private MessageSender messageSender;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    public UUID createRecipe(@Valid RecipeRequest recipeRequest) {

        final UUID uuid = UUID.randomUUID();
        RecipeMessageDTO recipeMessageDTO = RecipeMessageDTO.of(recipeRequest);
        recipeMessageDTO.setId(UUID.randomUUID());

        messageSender.sendMessage(RabbitMQConfig.RECIPE_QUEUE_NAME, recipeMessageDTO);

//        // Send the recipeDTO to the RabbitMQ exchange with routing key
//        rabbitTemplate.convertAndSend(
//                RabbitMQConfig.RECIPE_QUEUE_NAME,
//                recipeMessageDTO
//        );

       return uuid;
    }
}
