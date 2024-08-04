package com.abnamro.recipes_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
public class Recipes {

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

    @Column(columnDefinition = "tsvector")
    private String instructions_tsv;

    @ManyToMany
    @JoinTable(
            name = "recipe_ingredients",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<Ingredients> ingredients;

}
