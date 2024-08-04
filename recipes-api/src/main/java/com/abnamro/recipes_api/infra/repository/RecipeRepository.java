package com.abnamro.recipes_api.infra.repository;

import com.abnamro.recipes_api.model.Recipes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipes, Long>, RecipeRepositoryCustom {

}
