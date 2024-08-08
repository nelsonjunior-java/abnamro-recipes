package com.abnamro.recipes_api.unit.service;

import com.abnamro.recipes_api.config.RabbitMQConfig;
import com.abnamro.recipes_api.controller.request.CreateIngredientRequest;
import com.abnamro.recipes_api.infra.messaging.Message;
import com.abnamro.recipes_api.infra.messaging.MessageSender;
import com.abnamro.recipes_api.infra.messaging.dto.IngredientMessageDTO;
import com.abnamro.recipes_api.infra.repository.IngredientRepository;
import com.abnamro.recipes_api.model.Ingredients;
import com.abnamro.recipes_api.service.IngredientService;
import com.abnamro.recipes_api.service.exception.IngredientNotFoundException;
import com.abnamro.recipes_api.unit.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private IngredientService ingredientService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Save method tests
    @Test
    public void testSave_Success() {
        // Given
        final String ingredientName = "Salt";
        final CreateIngredientRequest request = TestUtils.createIngredientRequest(ingredientName);

        when(ingredientRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(ingredientRepository.save(any(Ingredients.class))).thenReturn(new Ingredients());

        // When
        final UUID result = ingredientService.save(request);

        // Then
        assertNotNull(result);
        verify(messageSender, times(1)).sendMessage(eq(RabbitMQConfig.INGREDIENT_QUEUE_NAME), any(IngredientMessageDTO.class));
    }

    @Test
    public void testSave_DuplicateIngredientName_ThrowsException() {
        // Given
        final String ingredientName = "Salt";
        final CreateIngredientRequest request = TestUtils.createIngredientRequest(ingredientName);

        when(ingredientRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // When
        assertThrows(IllegalArgumentException.class, () -> ingredientService.save(request));

        // Then
        verify(messageSender, never()).sendMessage(anyString(), any(Message.class));
    }

    // Delete method tests
    @Test
    public void testDelete_Success() {
        // Given
        final UUID ingredientUuid = UUID.randomUUID();
        when(ingredientRepository.existsByUuid(ingredientUuid)).thenReturn(true);

        // When
        final boolean result = ingredientService.delete(ingredientUuid.toString());

        // Then
        assertTrue(result);
        verify(ingredientRepository, times(1)).deleteByUuid(ingredientUuid);
    }

    @Test
    public void testDelete_NotFound() {
        // Given
        final UUID ingredientUuid = UUID.randomUUID();
        when(ingredientRepository.existsByUuid(ingredientUuid)).thenReturn(false);

        // When
        final boolean result = ingredientService.delete(ingredientUuid.toString());

        // Then
        assertFalse(result);
        verify(ingredientRepository, never()).deleteByUuid(any(UUID.class));
    }

    @Test
    public void testDelete_InvalidUuid_ThrowsException() {
        // Given
        final String invalidUuid = "invalid-uuid";

        // When
        assertThrows(RuntimeException.class, () -> ingredientService.delete(invalidUuid));

        // Then
        verify(ingredientRepository, never()).deleteByUuid(any(UUID.class));
    }

    // FindByUuid method test
    @Test
    public void testFindByUuid_Success() {
        // Given
        final UUID ingredientUuid = UUID.randomUUID();
        final Ingredients ingredient = TestUtils.createIngredient(ingredientUuid, "Salt");

        when(ingredientRepository.findByUuid(ingredientUuid)).thenReturn(Optional.of(ingredient));

        // When
        final Ingredients result = ingredientService.findByUuid(ingredientUuid.toString());

        // Then
        assertNotNull(result);
        assertEquals(ingredientUuid, result.getUuid());
    }

    @Test
    public void testFindByUuid_NotFound() {
        // Given
        final UUID ingredientUuid = UUID.randomUUID();
        when(ingredientRepository.findByUuid(ingredientUuid)).thenReturn(Optional.empty());

        // Then
        assertThrows(IngredientNotFoundException.class, () -> ingredientService.findByUuid(ingredientUuid.toString()));
    }

    // FindAll method test
    @Test
    public void testFindAll_Success() {
        // Given
        final Pageable pageable = PageRequest.of(0, 10);
        final List<Ingredients> ingredientsList = List.of(new Ingredients(), new Ingredients());
        final Page<Ingredients> ingredientsPage = new PageImpl<>(ingredientsList);

        when(ingredientRepository.findAll(pageable)).thenReturn(ingredientsPage);

        // When
        final Page<Ingredients> result = ingredientService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }
}
