package com.abnamro.recipes_consumer.infra.messaging.impl;

import com.abnamro.recipes_consumer.infra.messaging.MessageConsumer;
import com.abnamro.recipes_consumer.infra.messaging.dto.RecipeMessageDTO;
import com.abnamro.recipes_consumer.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecipeMessageConsumer implements MessageConsumer<RecipeMessageDTO> {

    @Autowired
    RecipeService recipeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void consumeMessage(RecipeMessageDTO recipeMessageDTO) {

            log.info("New message consumed. method=consumeMessage, recipe={}", recipeMessageDTO);

            recipeService.save(recipeMessageDTO);
    }
}
