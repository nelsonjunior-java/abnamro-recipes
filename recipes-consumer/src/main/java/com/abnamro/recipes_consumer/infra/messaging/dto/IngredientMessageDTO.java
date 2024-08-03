package com.abnamro.recipes_consumer.infra.messaging.dto;

import com.abnamro.recipes_consumer.infra.messaging.Message;
import lombok.Data;

import java.util.UUID;

@Data
public class IngredientMessageDTO implements Message {

    private UUID uuid;
    private String name;
}
