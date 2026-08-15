package com.tiagofsps.hunting_fishing_store.catalog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.CategoryCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.CategoryResponse;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryNotFoundException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setup() {
        category = new Category();
        category.setId(1L);
        category.setName("Fishing");
        category.setSlug("fishing");
        category.setDescription("Fishing gear");
    }

    @Test
    @DisplayName("Should return category when id exists")
    void shouldFindCategoryById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("Fishing", response.name());
        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when category does not exist")
    void shouldThrowWhenCategoryDoesNotExist() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.findById(1L));
        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should create category")
    void shouldCreateCategory() {
        CategoryCreateRequest request = new CategoryCreateRequest("Fishing", "fishing", "Fishing gear", null);
        when(categoryRepository.existsBySlug("fishing")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.create(request);

        assertEquals("Fishing", response.name());
        verify(categoryRepository).existsBySlug("fishing");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should create subcategory")
    void shouldCreateSubcategory() {
        Category parent = new Category();
        parent.setId(1L);
        
        CategoryCreateRequest request = new CategoryCreateRequest("Rods", "rods", "Fishing rods", 1L);
        
        when(categoryRepository.existsBySlug("rods")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryResponse response = categoryService.create(request);

        assertEquals("Rods", response.name());
        assertEquals(1L, response.parentCategoryId());
    }

    @Test
    @DisplayName("Should delete category")
    void shouldDeleteCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }
}
