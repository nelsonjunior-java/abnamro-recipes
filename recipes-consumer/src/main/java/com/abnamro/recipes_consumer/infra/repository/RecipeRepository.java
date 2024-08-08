package com.abnamro.recipes_consumer.infra.repository;

import com.abnamro.recipes_consumer.model.Recipes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipes, Long> {

    Boolean existsByUuid(UUID recipeUuid);

    Recipes findByUuid(UUID recipeUuid);

    @Query("SELECT r FROM Recipes r LEFT JOIN FETCH r.ingredients WHERE r.uuid = :uuid")
    Recipes findByUuidWithIngredients(@Param("uuid") UUID uuid);
}
