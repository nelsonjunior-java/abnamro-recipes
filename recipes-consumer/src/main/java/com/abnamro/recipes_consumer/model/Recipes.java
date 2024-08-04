package com.abnamro.recipes_consumer.model;


import com.abnamro.recipes_consumer.infra.messaging.dto.RecipeMessageDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
public class Recipes implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "is_vegetarian", nullable = false)
    private boolean isVegetarian;

    @Column(nullable = false)
    private Integer servings;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String instructions;

    @ManyToMany
    @JoinTable(
            name = "recipe_ingredients",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<Ingredients> ingredients;


    public static Recipes of(RecipeMessageDTO recipeMessageDTO, List<Ingredients> ingredientsList) {

        Recipes recipes = new Recipes();
        recipes.setUuid(recipeMessageDTO.getId());
        recipes.setName(recipeMessageDTO.getName());
        recipes.setServings(recipeMessageDTO.getServings());
        recipes.setInstructions(recipeMessageDTO.getInstructions());
        recipes.setVegetarian(recipeMessageDTO.getVegetarian());
        recipes.setIngredients(new HashSet<>(ingredientsList));

        return recipes;
    }
}


