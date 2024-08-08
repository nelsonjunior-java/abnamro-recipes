package com.abnamro.recipes_api.infra.repository;

import com.abnamro.recipes_api.model.Recipes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecipeRepository extends JpaRepository<Recipes, Long>, RecipeRepositoryCustom {

    Optional<Recipes> findByUuid(UUID uuid);
}
