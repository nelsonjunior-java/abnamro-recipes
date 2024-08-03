package com.abnamro.recipes_consumer.service;

import com.abnamro.recipes_consumer.config.RabbitMQConfig;
import com.abnamro.recipes_consumer.infra.messaging.dto.RecipeMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RecipeConsumerService {

    @RabbitListener(queues = RabbitMQConfig.RECIPE_QUEUE_NAME)
    public void receiveMessage(RecipeMessageDTO recipeMessageDTO) {

        log.info("New message consumed. method=createRecipe, recipe={}", recipeMessageDTO);

        // Process the message here
        System.out.println("Received message: " + recipeMessageDTO);
    }
}
