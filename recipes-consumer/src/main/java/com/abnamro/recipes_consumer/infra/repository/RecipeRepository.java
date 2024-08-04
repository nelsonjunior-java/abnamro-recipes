package com.abnamro.recipes_consumer.infra.repository;

import com.abnamro.recipes_consumer.model.Recipes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipes, Long> {

}
