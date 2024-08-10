package com.abnamro.recipes_api.infra.repository.helper;

/**
 * Enum representing the various search criteria fields that can be used when querying recipes.
 * <p>
 * Each enum constant corresponds to a specific field in the recipe database and is associated with a string
 * representing the corresponding database column or parameter name.
 * </p>
 */
public enum SearchCriteria {
    VEGETARIAN("is_vegetarian"),
    SERVINGS("servings"),
    INCLUDE_INGREDIENT("includeIngredient"),
    EXCLUDE_INGREDIENT("excludeIngredient"),
    INSTRUCTION_TEXT("instructionText");

    private final String field;

    SearchCriteria(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
