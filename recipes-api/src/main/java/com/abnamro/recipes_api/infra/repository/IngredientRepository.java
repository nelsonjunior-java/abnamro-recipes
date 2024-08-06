package com.abnamro.recipes_api.infra.repository;

import com.abnamro.recipes_api.model.Ingredients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredients, Long> {

    Optional<Ingredients> findByUuid(UUID uuid);

    void deleteByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN TRUE ELSE FALSE END FROM Ingredients i WHERE LOWER(i.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);
}
