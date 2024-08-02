package com.abnamro.recipes_consumer.config;

import com.abnamro.recipes_consumer.service.RecipeConsumerService;
import com.abnamro.recipes_consumer.service.dto.RecipeDTO;
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

    @Bean
    public Queue recipeQueue() {
        return new Queue(RECIPE_QUEUE_NAME, true);
    }

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                                    MessageListenerAdapter messageListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(RECIPE_QUEUE_NAME);
        container.setMessageListener(messageListenerAdapter);
        return container;
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(RecipeConsumerService recipeConsumerService, Jackson2JsonMessageConverter messageConverter) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(recipeConsumerService, "receiveMessage");
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
        // Map the class name used by the producer to the consumer's class
        idClassMapping.put("com.abnamro.recipes_api.service.dto.RecipeDTO",
                com.abnamro.recipes_consumer.service.dto.RecipeDTO.class);
        classMapper.setIdClassMapping(idClassMapping);
        // Ensure all relevant packages are trusted
        classMapper.setTrustedPackages("com.abnamro.recipes_api.service.dto",
                "com.abnamro.recipes_consumer.service.dto",
                "java.util");
        return classMapper;
    }

}
