package com.abnamro.recipes_api.infra.repository;

import com.abnamro.recipes_api.controller.request.RecipeSearchRequest;
import com.abnamro.recipes_api.model.Recipes;

import java.util.List;

/**
 * Custom repository interface for performing advanced recipe searches based on various criteria.
 * <p>
 * This interface defines a method for finding recipes that match specific search criteria provided by a {@link RecipeSearchRequest}.
 * </p>
 */
public interface RecipeRepositoryCustom {
    List<Recipes> findRecipesBySearchCriteria(RecipeSearchRequest searchRequest);

}
