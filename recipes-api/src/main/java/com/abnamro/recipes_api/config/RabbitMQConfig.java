package com.abnamro.recipes_api.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;


@Configuration
public class RabbitMQConfig {

    public static final String RECIPE_QUEUE_NAME = "recipes_queue";
    public static final String INGREDIENT_QUEUE_NAME = "ingredients_queue";

    @Bean
    public Queue recipesQueue() {
        return new Queue(RECIPE_QUEUE_NAME, true);
    }

    @Bean
    public Queue ingredientsQueue() {
        return new Queue(INGREDIENT_QUEUE_NAME, true);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
