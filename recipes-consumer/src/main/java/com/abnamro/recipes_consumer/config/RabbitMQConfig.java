package com.abnamro.recipes_consumer.config;

import com.abnamro.recipes_consumer.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_consumer.infra.messaging.dto.RecipeMessageDTO;
import com.abnamro.recipes_consumer.infra.messaging.impl.IngredientMessageConsumer;
import com.abnamro.recipes_consumer.infra.messaging.impl.RecipeMessageConsumer;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String RECIPE_QUEUE_NAME = "recipes_queue";
    public static final String INGREDIENT_QUEUE_NAME = "ingredients_queue";

    private static final String DEFAULT_LISTENER_METHOD_NAME = "consumeMessage";

    @Bean
    public Queue recipeQueue() {
        return new Queue(RECIPE_QUEUE_NAME, true);
    }

    @Bean
    public Queue ingredientQueue() {
        return new Queue(INGREDIENT_QUEUE_NAME, true);
    }

    @Bean
    public SimpleMessageListenerContainer recipeContainer(ConnectionFactory connectionFactory, MessageListenerAdapter recipeMessageListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(RECIPE_QUEUE_NAME);
        container.setMessageListener(recipeMessageListenerAdapter);
        return container;
    }

    @Bean
    public SimpleMessageListenerContainer ingredientContainer(ConnectionFactory connectionFactory, MessageListenerAdapter ingredientMessageListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(INGREDIENT_QUEUE_NAME);
        container.setMessageListener(ingredientMessageListenerAdapter);
        return container;
    }

    @Bean
    public MessageListenerAdapter recipeMessageListenerAdapter(RecipeMessageConsumer recipeConsumerService, Jackson2JsonMessageConverter messageConverter) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(recipeConsumerService, DEFAULT_LISTENER_METHOD_NAME);
        adapter.setMessageConverter(messageConverter);
        return adapter;
    }

    @Bean
    public MessageListenerAdapter ingredientMessageListenerAdapter(IngredientMessageConsumer ingredientConsumerService, Jackson2JsonMessageConverter messageConverter) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(ingredientConsumerService, DEFAULT_LISTENER_METHOD_NAME);
        adapter.setMessageConverter(messageConverter);
        return adapter;
    }


    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper());
        return converter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();

        // Map the class name used by the producer to the consumer's classes
        idClassMapping.put("com.abnamro.recipes_api.infra.messaging.dto.RecipeMessageDTO",
                RecipeMessageDTO.class);

        idClassMapping.put("com.abnamro.recipes_api.infra.messaging.dto.IngredientMessageDTO",
                IngredientMessageDTO.class);

        classMapper.setIdClassMapping(idClassMapping);
        // Ensure all relevant packages are trusted
        classMapper.setTrustedPackages("com.abnamro.recipes_api.service.dto",
                "com.abnamro.recipes_consumer.infra.messaging.dto",
                "java.util");
        return classMapper;
    }

}
