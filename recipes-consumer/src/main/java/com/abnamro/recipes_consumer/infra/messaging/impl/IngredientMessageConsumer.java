package com.abnamro.recipes_consumer.infra.messaging.impl;

import com.abnamro.recipes_consumer.config.RabbitMQConfig;
import com.abnamro.recipes_consumer.infra.messaging.Message;
import com.abnamro.recipes_consumer.infra.messaging.MessageConsumer;
import com.abnamro.recipes_consumer.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_consumer.service.IngredientsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Component
public class IngredientMessageConsumer implements MessageConsumer<IngredientMessageDTO> {

    @Autowired
    IngredientsService ingredientsService;

    @Override
    @RabbitListener(queues = RabbitMQConfig.INGREDIENT_QUEUE_NAME)
    public void consumeMessage(IngredientMessageDTO ingredientMessageDTO) {

        log.info("New message consumed. method=consumeMessage, recipe={}", ingredientMessageDTO);

        ingredientsService.save(ingredientMessageDTO);
    }

}
