package com.abnamro.recipes_api.model;

import com.abnamro.recipes_api.infra.messaging.dto.IngredientMessageDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Ingredients {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, length = 255)
    private String name;

    public static Ingredients of(IngredientMessageDTO dto) {
        Ingredients ingredients = new Ingredients();
        ingredients.setUuid(dto.getUuid());
        ingredients.setName(dto.getName());
        return ingredients;
    }
}
