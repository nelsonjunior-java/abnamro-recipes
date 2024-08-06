package com.abnamro.recipes_api.service;

import com.abnamro.recipes_api.config.RabbitMQConfig;
import com.abnamro.recipes_api.controller.reponse.RecipeResponse;
import com.abnamro.recipes_api.controller.request.RecipeRequest;
import com.abnamro.recipes_api.controller.request.RecipeSearchRequest;
import com.abnamro.recipes_api.infra.messaging.MessageSender;
import com.abnamro.recipes_api.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_api.infra.messaging.dto.RecipeMessageDTO;
import com.abnamro.recipes_api.infra.repository.IngredientRepository;
import com.abnamro.recipes_api.infra.repository.RecipeRepository;
import com.abnamro.recipes_api.model.Recipes;
import com.abnamro.recipes_api.service.exception.IngredientNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.abnamro.recipes_api.model.Ingredients;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecipeService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    RecipeRepository recipeRepository;

    public UUID createRecipe(@Valid RecipeRequest recipeRequest) {

        // Validate if Ingredients exist
        final List<Ingredients> ingredients = recipeRequest.getIngredientIds().stream()
                .map(this::validateAndFetchIngredient)
                .toList();

        // Convert Ingredients to IngredientMessageDTOs
        final List<IngredientMessageDTO> ingredientMessageDTOs = ingredients.stream()
                .map(IngredientMessageDTO::of)
                .collect(Collectors.toList());

        final UUID uuid = UUID.randomUUID();
        RecipeMessageDTO recipeMessageDTO = RecipeMessageDTO.of(recipeRequest, ingredientMessageDTOs);
        recipeMessageDTO.setId(uuid);

        messageSender.sendMessage(RabbitMQConfig.RECIPE_QUEUE_NAME, recipeMessageDTO);

        return uuid;
    }

    private Ingredients validateAndFetchIngredient(final String ingredientUuid) {

        final UUID uuid = UUID.fromString(ingredientUuid);

        final Optional<Ingredients> optionalIngredient = ingredientRepository.findByUuid(uuid);
        if (optionalIngredient.isEmpty()) {
            throw new IngredientNotFoundException("Ingredient with UUID " + ingredientUuid + " does not exist");
        }
        return optionalIngredient.get();
    }

    public List<Recipes> searchRecipes(RecipeSearchRequest searchRequest) {

        return recipeRepository.findRecipesBySearchCriteria(searchRequest);

    }
}
