package com.abnamro.recipes_api.infra.repository.helper;

import com.abnamro.recipes_api.controller.request.RecipeSearchRequest;

/**
 * Helper class to build a dynamic SQL query based on the search criteria provided in a {@link RecipeSearchRequest}.
 * <p>
 * This class encapsulates the logic for constructing the SQL query string by appending conditions
 * corresponding to the various search criteria such as vegetarian status, servings, ingredients to include or exclude,
 * and specific text in the recipe instructions.
 * </p>
 */
public class RecipeQueryBuilder {

    private final StringBuilder sql;
    private final RecipeSearchRequest searchRequest;

    public RecipeQueryBuilder(RecipeSearchRequest searchRequest) {
        this.sql = new StringBuilder("SELECT r.* FROM recipes r WHERE 1=1");
        this.searchRequest = searchRequest;
    }

    /**
     * Builds the SQL query string by appending conditions based on the search criteria.
     *
     * @return the constructed SQL query string
     */
    public String build() {
        addVegetarianPredicate();
        addServingsPredicate();
        addIncludeIngredientPredicate();
        addExcludeIngredientPredicate();
        addInstructionTextPredicate();
        return sql.toString();
    }

    private void addVegetarianPredicate() {
        if (searchRequest.getIsVegetarian() != null) {
            sql.append(" AND r.is_vegetarian = :isVegetarian");
        }
    }

    private void addServingsPredicate() {
        if (searchRequest.getServings() != null) {
            sql.append(" AND r.servings = :servings");
        }
    }

    private void addIncludeIngredientPredicate() {
        if (searchRequest.getIncludeIngredient() != null) {
            sql.append(" AND r.id IN (SELECT ri.recipe_id FROM recipe_ingredients ri JOIN ingredients i ON ri.ingredient_id = i.id WHERE i.name = :includeIngredient)");
        }
    }

    private void addExcludeIngredientPredicate() {
        if (searchRequest.getExcludeIngredient() != null) {
            sql.append(" AND r.id NOT IN (SELECT ri.recipe_id FROM recipe_ingredients ri JOIN ingredients i ON ri.ingredient_id = i.id WHERE i.name = :excludeIngredient)");
        }
    }

    private void addInstructionTextPredicate() {
        if (searchRequest.getInstructionText() != null) {
            sql.append(" AND r.instructions_tsv @@ plainto_tsquery(:instructionText)");
        }
    }
}
