package com.abnamro.recipes_api.infra.repository.impl;

import com.abnamro.recipes_api.controller.request.RecipeSearchRequest;
import com.abnamro.recipes_api.infra.repository.RecipeRepositoryCustom;
import com.abnamro.recipes_api.model.Recipes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class RecipeRepositoryCustomImpl implements RecipeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Recipes> findRecipesBySearchCriteria(final RecipeSearchRequest searchRequest) {
        String sql = buildSqlQuery(searchRequest);
        Query query = createQuery(sql, searchRequest);
        logGeneratedSqlQuery(sql);
        return executeQuery(query);
    }

    private String buildSqlQuery(RecipeSearchRequest searchRequest) {
        StringBuilder sql = new StringBuilder("SELECT r.* FROM recipes r WHERE 1=1");
        addVegetarianPredicate(sql, searchRequest);
        addServingsPredicate(sql, searchRequest);
        addIncludeIngredientPredicate(sql, searchRequest);
        addExcludeIngredientPredicate(sql, searchRequest);
        addInstructionTextPredicate(sql, searchRequest);
        return sql.toString();
    }

    private void addVegetarianPredicate(StringBuilder sql, RecipeSearchRequest searchRequest) {
        if (searchRequest.getIsVegetarian() != null) {
            sql.append(" AND r.is_vegetarian = :isVegetarian");
        }
    }

    private void addServingsPredicate(StringBuilder sql, RecipeSearchRequest searchRequest) {
        if (searchRequest.getServings() != null) {
            sql.append(" AND r.servings = :servings");
        }
    }

    private void addIncludeIngredientPredicate(StringBuilder sql, RecipeSearchRequest searchRequest) {
        if (searchRequest.getIncludeIngredient() != null) {
            sql.append(" AND r.id IN (SELECT ri.recipe_id FROM recipe_ingredients ri JOIN ingredients i ON ri.ingredient_id = i.id WHERE i.name = :includeIngredient)");
        }
    }

    private void addExcludeIngredientPredicate(StringBuilder sql, RecipeSearchRequest searchRequest) {
        if (searchRequest.getExcludeIngredient() != null) {
            sql.append(" AND r.id NOT IN (SELECT ri.recipe_id FROM recipe_ingredients ri JOIN ingredients i ON ri.ingredient_id = i.id WHERE i.name = :excludeIngredient)");
        }
    }

    private void addInstructionTextPredicate(StringBuilder sql, RecipeSearchRequest searchRequest) {
        if (searchRequest.getInstructionText() != null) {
            sql.append(" AND r.instructions_tsv @@ plainto_tsquery(:instructionText)");
        }
    }

    private Query createQuery(String sql, RecipeSearchRequest searchRequest) {
        Query query = entityManager.createNativeQuery(sql, Recipes.class);
        setQueryParameters(query, searchRequest);
        return query;
    }

    private void setQueryParameters(Query query, RecipeSearchRequest searchRequest) {
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

    private List<Recipes> executeQuery(Query query) {
        return query.getResultList();
    }

    private void logGeneratedSqlQuery(String sql) {
        log.info("Generated SQL Query: {}", sql);
    }


}
