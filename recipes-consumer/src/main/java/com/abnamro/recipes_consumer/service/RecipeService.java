package com.abnamro.recipes_consumer.service;

import com.abnamro.recipes_consumer.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_consumer.infra.messaging.dto.RecipeMessageDTO;
import com.abnamro.recipes_consumer.infra.repository.IngredientRepository;
import com.abnamro.recipes_consumer.infra.repository.RecipeRepository;
import com.abnamro.recipes_consumer.model.Ingredients;
import com.abnamro.recipes_consumer.model.Recipes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    public void save(RecipeMessageDTO recipeMessageDTO){

        // Extract UUIDs from the incoming RecipeMessageDTO
        List<UUID> ingredientUuids = recipeMessageDTO.getIngredientIds().stream()
                .map(IngredientMessageDTO::getUuid)
                .toList();

        final List<Ingredients> ingredientsList = findByUuids(ingredientUuids);

        final Recipes recipes = Recipes.of(recipeMessageDTO, ingredientsList);

        log.info("Storing the recipe in the database. method=save, recipe={}", recipes);
        recipeRepository.save(recipes);
    }

    public List<Ingredients> findByUuids(List<UUID> uuids) {
        return ingredientRepository.findByUuidIn(uuids);
    }
}
