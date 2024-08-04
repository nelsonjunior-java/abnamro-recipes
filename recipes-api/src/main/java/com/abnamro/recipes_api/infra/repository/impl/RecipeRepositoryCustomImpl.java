package com.abnamro.recipes_api.infra.repository.impl;

import com.abnamro.recipes_api.controller.request.RecipeSearchRequest;
import com.abnamro.recipes_api.infra.repository.RecipeRepositoryCustom;
import com.abnamro.recipes_api.model.Ingredients;
import com.abnamro.recipes_api.model.Recipes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class RecipeRepositoryCustomImpl implements RecipeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Recipes> findRecipesBySearchCriteria(final RecipeSearchRequest searchRequest) {

        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Recipes> query = cb.createQuery(Recipes.class);
        final Root<Recipes> recipe = query.from(Recipes.class);

        final List<Predicate> predicates = new ArrayList<>();

        if (searchRequest.getIsVegetarian() != null) {
            predicates.add(cb.equal(recipe.get("isVegetarian"), searchRequest.getIsVegetarian()));
        }

        if (searchRequest.getServings() != null) {
            predicates.add(cb.equal(recipe.get("servings"), searchRequest.getServings()));
        }

        if (searchRequest.getIncludeIngredient() != null) {
            Join<Recipes, Ingredients> ingredientJoin = recipe.join("ingredients");
            predicates.add(cb.equal(ingredientJoin.get("name"), searchRequest.getIncludeIngredient()));
        }

        if (searchRequest.getExcludeIngredient() != null) {
            Join<Recipes, Ingredients> ingredientJoin = recipe.join("ingredients");
            predicates.add(cb.not(cb.equal(ingredientJoin.get("name"), searchRequest.getExcludeIngredient())));
        }

        if (searchRequest.getInstructionText() != null) {
            // Use PostgreSQL's full-text search to filter recipes based on instructions.
            // Convert the search text to `tsquery` format with `plainto_tsquery`.
            // Check if the `instructions_tsv` column, which stores precomputed `tsvector`,
            // contains the search text. `cb.isTrue` ensures the search term matches the vector.
            predicates.add(cb.isTrue(cb.function("to_tsvector", String.class, recipe.get("instructions_tsv"))
                    .in(cb.function("plainto_tsquery", String.class, cb.literal(searchRequest.getInstructionText())))));
        }

        query.select(recipe).where(predicates.toArray(new Predicate[0]));

        // Log the generated query for debugging purposes
        String sqlQuery = entityManager.createQuery(query).unwrap(org.hibernate.query.Query.class).getQueryString();
        log.info("Generated SQL Query: {}", sqlQuery);

        return entityManager.createQuery(query).getResultList();

    }
}
