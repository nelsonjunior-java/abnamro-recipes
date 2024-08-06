package com.abnamro.recipes_api.unit.infra.repository.impl;

import com.abnamro.recipes_api.controller.request.RecipeSearchRequest;
import com.abnamro.recipes_api.infra.repository.impl.RecipeRepositoryCustomImpl;
import com.abnamro.recipes_api.model.Recipes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RecipeRepositoryCustomImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query mockQuery;

    @InjectMocks
    private RecipeRepositoryCustomImpl recipeRepository;

    private Recipes recipe1;
    private Recipes recipe2;

    @BeforeEach
    public void setUp() {
        recipe1 = new Recipes();
        recipe1.setId(1l);
        recipe1.setName("Recipe 1");

        recipe2 = new Recipes();
        recipe2.setId(2l);
        recipe2.setName("Recipe 2");

        // Mock the EntityManager and Query
        when(entityManager.createNativeQuery(anyString(), eq(Recipes.class))).thenReturn(mockQuery);
    }

    private void setUpQueryResults(List<Recipes> results) {
        when(mockQuery.getResultList()).thenReturn(results);
    }

    @Test
    public void testFindRecipesBySearchCriteria_NoFilters() {
        setUpQueryResults(Arrays.asList(recipe1, recipe2));

        RecipeSearchRequest request = new RecipeSearchRequest();
        List<Recipes> recipes = recipeRepository.findRecipesBySearchCriteria(request);

        verify(entityManager).createNativeQuery(anyString(), eq(Recipes.class));
        verify(mockQuery).getResultList();
        assertEquals(2, recipes.size());
        assertEquals(Arrays.asList(recipe1, recipe2), recipes);
    }

    @Test
    public void testFindRecipesBySearchCriteria_IsVegetarian() {
        setUpQueryResults(Arrays.asList(recipe1));

        RecipeSearchRequest request = new RecipeSearchRequest();
        request.setIsVegetarian(true);

        List<Recipes> recipes = recipeRepository.findRecipesBySearchCriteria(request);

        verify(mockQuery).setParameter("isVegetarian", true);
        verify(mockQuery).getResultList();
        assertEquals(1, recipes.size());
        assertEquals(recipe1, recipes.get(0));
    }

    @Test
    public void testFindRecipesBySearchCriteria_Servings() {
        setUpQueryResults(Arrays.asList(recipe2));

        RecipeSearchRequest request = new RecipeSearchRequest();
        request.setServings(4);

        List<Recipes> recipes = recipeRepository.findRecipesBySearchCriteria(request);

        verify(mockQuery).setParameter("servings", 4);
        verify(mockQuery).getResultList();
        assertEquals(1, recipes.size());
        assertEquals(recipe2, recipes.get(0));
    }

    @Test
    public void testFindRecipesBySearchCriteria_IncludeIngredient() {
        setUpQueryResults(Arrays.asList(recipe1));

        RecipeSearchRequest request = new RecipeSearchRequest();
        request.setIncludeIngredient("Eggs");

        List<Recipes> recipes = recipeRepository.findRecipesBySearchCriteria(request);

        verify(mockQuery).setParameter("includeIngredient", "Eggs");
        verify(mockQuery).getResultList();
        assertEquals(1, recipes.size());
        assertEquals(recipe1, recipes.get(0));
    }

    @Test
    public void testFindRecipesBySearchCriteria_ExcludeIngredient() {
        setUpQueryResults(Arrays.asList(recipe2));

        RecipeSearchRequest request = new RecipeSearchRequest();
        request.setExcludeIngredient("Lettuce");

        List<Recipes> recipes = recipeRepository.findRecipesBySearchCriteria(request);

        verify(mockQuery).setParameter("excludeIngredient", "Lettuce");
        verify(mockQuery).getResultList();
        assertEquals(1, recipes.size());
        assertEquals(recipe2, recipes.get(0));
    }

    @Test
    public void testFindRecipesBySearchCriteria_InstructionText() {
        setUpQueryResults(Arrays.asList(recipe1));

        RecipeSearchRequest request = new RecipeSearchRequest();
        request.setInstructionText("mix well");

        List<Recipes> recipes = recipeRepository.findRecipesBySearchCriteria(request);

        verify(mockQuery).setParameter("instructionText", "mix well");
        verify(mockQuery).getResultList();
        assertEquals(1, recipes.size());
        assertEquals(recipe1, recipes.get(0));
    }

    @Test
    public void testFindRecipesBySearchCriteria_AllFilters() {
        setUpQueryResults(Arrays.asList(recipe1));

        RecipeSearchRequest request = new RecipeSearchRequest();
        request.setIsVegetarian(true);
        request.setServings(4);
        request.setIncludeIngredient("Eggs");
        request.setExcludeIngredient("Lettuce");
        request.setInstructionText("mix well");

        List<Recipes> recipes = recipeRepository.findRecipesBySearchCriteria(request);

        verify(mockQuery).setParameter("isVegetarian", true);
        verify(mockQuery).setParameter("servings", 4);
        verify(mockQuery).setParameter("includeIngredient", "Eggs");
        verify(mockQuery).setParameter("excludeIngredient", "Lettuce");
        verify(mockQuery).setParameter("instructionText", "mix well");
        verify(mockQuery).getResultList();
        assertEquals(1, recipes.size());
        assertEquals(recipe1, recipes.get(0));
    }
}
