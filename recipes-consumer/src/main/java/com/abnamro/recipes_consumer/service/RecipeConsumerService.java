package com.abnamro.recipes_consumer.service;

import com.abnamro.recipes_consumer.config.RabbitMQConfig;
import com.abnamro.recipes_consumer.service.dto.RecipeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RecipeConsumerService {

    @RabbitListener(queues = RabbitMQConfig.RECIPE_QUEUE_NAME)
    public void receiveMessage(RecipeDTO recipeDTO) {

        log.info("New message consumed. method=createRecipe, recipe={}", recipeDTO);

        // Process the message here
        System.out.println("Received message: " + recipeDTO);
    }
}
