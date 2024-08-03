package com.abnamro.recipes_consumer.infra.messaging.impl;

import com.abnamro.recipes_consumer.infra.messaging.Message;
import com.abnamro.recipes_consumer.infra.messaging.MessageConsumer;
import com.abnamro.recipes_consumer.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_consumer.infra.messaging.dto.RecipeMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecipeMessageConsumer implements MessageConsumer<RecipeMessageDTO> {

    @Override
    public void consumeMessage(RecipeMessageDTO recipeMessageDTO) {

        log.info("New message consumed. method=consumeMessage, recipe={}", recipeMessageDTO);
    }
}
