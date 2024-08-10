package com.abnamro.recipes_api.infra.repository.impl;

import com.abnamro.recipes_api.controller.request.RecipeSearchRequest;
import com.abnamro.recipes_api.infra.repository.RecipeRepositoryCustom;
import com.abnamro.recipes_api.infra.repository.helper.RecipeQueryBuilder;
import com.abnamro.recipes_api.model.Recipes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Custom implementation of the {@link RecipeRepositoryCustom} interface for searching recipes based on custom criteria.
 * <p>
 * This class builds and executes SQL queries dynamically based on the provided search criteria.
 * </p>
 */
@Repository
@Slf4j
public class RecipeRepositoryCustomImpl implements RecipeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Searches for recipes based on the criteria specified in the {@link RecipeSearchRequest}.
     *
     * @param searchRequest the search criteria
     * @return a list of recipes that match the search criteria
     */
    @Override
    public List<Recipes> findRecipesBySearchCriteria(final RecipeSearchRequest searchRequest) {
        final String sql = new RecipeQueryBuilder(searchRequest).build();
        final Query query = createQuery(sql, searchRequest);
        logGeneratedSqlQuery(sql);
        return executeQuery(query);
    }

    private Query createQuery(final String sql,final RecipeSearchRequest searchRequest) {
        final Query query = entityManager.createNativeQuery(sql, Recipes.class);
        setQueryParameters(query, searchRequest);
        return query;
    }

    private void setQueryParameters(final Query query,final RecipeSearchRequest searchRequest) {

        if (searchRequest.getIsVegetarian() != null) {
            query.setParameter("isVegetarian", searchRequest.getIsVegetarian());
        }
        if (searchRequest.getServings() != null) {
            query.setParameter("servings", searchRequest.getServings());
        }
        if (searchRequest.getIncludeIngredient() != null) {
            query.setParameter("includeIngredient", searchRequest.getIncludeIngredient());
        }
        if (searchRequest.getExcludeIngredient() != null) {
            query.setParameter("excludeIngredient", searchRequest.getExcludeIngredient());
        }
        if (searchRequest.getInstructionText() != null) {
            query.setParameter("instructionText", searchRequest.getInstructionText());
        }
    }

    private List<Recipes> executeQuery(final Query query) {
        return query.getResultList();
    }

    private void logGeneratedSqlQuery(String sql) {
        log.info("Generated SQL Query: {}", sql);
    }

}