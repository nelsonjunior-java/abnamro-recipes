package com.abnamro.recipes_consumer.service;

import com.abnamro.recipes_consumer.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_consumer.infra.repository.IngredientRepository;
import com.abnamro.recipes_consumer.model.Ingredients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IngredientsService {

    @Autowired
    private IngredientRepository ingredientRepository;

    public void save(IngredientMessageDTO ingredientMessageDTO){

        final Ingredients ingredients = Ingredients.of(ingredientMessageDTO);

        log.info("Storing the ingredient in the database. method=save, ingredient={}", ingredientMessageDTO);
        ingredientRepository.save(ingredients);
    }
}
