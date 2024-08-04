package com.abnamro.recipes_api.service;

import com.abnamro.recipes_api.config.RabbitMQConfig;
import com.abnamro.recipes_api.controller.request.CreateIngredientRequest;
import com.abnamro.recipes_api.infra.messaging.MessageSender;
import com.abnamro.recipes_api.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_api.infra.repository.IngredientRepository;
import com.abnamro.recipes_api.model.Ingredients;
import com.abnamro.recipes_api.service.exception.IngredientNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@Transactional
public class IngredientService {

    private String name;

    @Autowired
    IngredientRepository ingredientRepository;

    @Autowired
    private MessageSender messageSender;

    public UUID save(CreateIngredientRequest createIngredientRequest) {

        final UUID uuid = UUID.randomUUID();
        IngredientMessageDTO ingredientMessageDTO = IngredientMessageDTO.of(createIngredientRequest);
        ingredientMessageDTO.setUuid(UUID.randomUUID());

        messageSender.sendMessage(RabbitMQConfig.INGREDIENT_QUEUE_NAME, ingredientMessageDTO);

        return uuid;
    }

    public boolean delete(String id) {

        try {
            final UUID uuid = UUID.fromString(id);

            // Check if the ingredient exists
            if (ingredientRepository.existsByUuid(uuid)) {
                ingredientRepository.deleteByUuid(uuid);
                log.info("Deleted ingredient with id={}", id);
                return true;
            } else {
                log.warn("Ingredient with id={} not found for deletion", id);
                return false;
            }
        } catch (Exception e) {
            log.error("Error occurred while deleting ingredient with id={}", id, e);
            throw new RuntimeException("Failed to delete ingredient", e);
        }
    }

    public Ingredients findByUuid(String uuidString) {

        final UUID uuid = UUID.fromString(uuidString);
        ingredientRepository.findByUuid(uuid);

        return ingredientRepository.findByUuid(uuid)
                .orElseThrow(() -> new IngredientNotFoundException("Ingredient not found!"));
    }

    public Page<Ingredients> findAll(Pageable pageable) {
        return ingredientRepository.findAll(pageable);
    }
}
