package com.abnamro.recipes_consumer.infra.repository;

import com.abnamro.recipes_consumer.model.Ingredients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredients, Long> {

    // Method to find ingredients by a list of UUIDs
    List<Ingredients> findByUuidIn(List<UUID> uuids);

    boolean existsByUuid(UUID uuid);

    Optional<Ingredients> findByUuid(UUID ingredientUuid);
}
