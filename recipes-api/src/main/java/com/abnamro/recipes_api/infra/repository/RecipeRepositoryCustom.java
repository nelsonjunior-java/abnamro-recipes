package com.abnamro.recipes_api.infra.repository;

import com.abnamro.recipes_api.controller.request.RecipeSearchRequest;
import com.abnamro.recipes_api.model.Recipes;

import java.util.List;

public interface RecipeRepositoryCustom {
    List<Recipes> findRecipesBySearchCriteria(RecipeSearchRequest searchRequest);

}
