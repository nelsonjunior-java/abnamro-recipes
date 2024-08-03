package com.abnamro.recipes_consumer.model;


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

@Entity
@Data
public class Recipe implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "is_vegetarian", nullable = false)
    private boolean isVegetarian;

    @Column(nullable = false)
    private Integer servings;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String instructions;

//    @Column(name = "instructions_tsv", columnDefinition = "TSVECTOR")
//    private String instructionsTsv; // This is typically not directly mapped but included for completeness

//    @ManyToMany
//    @JoinTable(
//            name = "recipe_ingredients",
//            joinColumns = @JoinColumn(name = "recipe_id"),
//            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
//    )
//
    //private Set<Ingredient> ingredients;


}


