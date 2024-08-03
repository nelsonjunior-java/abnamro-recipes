package com.abnamro.recipes_consumer.infra.repository;

import com.abnamro.recipes_consumer.model.Ingredients;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredients, Long> {
}
