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
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryNotFoundException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

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
        lenient().when(productRepository.countProductsByCategory()).thenReturn(List.of());
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
    @DisplayName("Should return all categories")
    void shouldReturnAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponse> result = categoryService.findAll();

        assertEquals(1, result.size());
        assertEquals("Fishing", result.getFirst().name());
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Should include productCount from the repository group query")
    void shouldIncludeProductCount() {
        when(productRepository.countProductsByCategory()).thenReturn(List.<Object[]>of(new Object[]{1L, 42L}));
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        CategoryResponse response = categoryService.findAll().getFirst();

        assertEquals(42L, response.productCount());
        verify(productRepository).countProductsByCategory();
    }

    @Test
    @DisplayName("Should default productCount to zero when category has no products")
    void shouldDefaultProductCountToZero() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        CategoryResponse response = categoryService.findAll().getFirst();

        assertEquals(0L, response.productCount());
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
    @DisplayName("Should throw exception when creating duplicated slug")
    void shouldThrowWhenCreatingDuplicatedSlug() {
        CategoryCreateRequest request = new CategoryCreateRequest("Fishing", "fishing", "Desc", null);
        when(categoryRepository.existsBySlug("fishing")).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.create(request));
        verify(categoryRepository, never()).save(any());
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
    @DisplayName("Should update existing category")
    void shouldUpdateCategory() {
        CategoryCreateRequest request = new CategoryCreateRequest("Updated", "updated", "New desc", null);
        
        when(categoryRepository.existsBySlugAndIdNot("updated", 1L)).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CategoryResponse response = categoryService.update(1L, request);

        assertEquals("Updated", response.name());
        assertEquals("updated", response.slug());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent category")
    void shouldThrowWhenUpdatingNonExistent() {
        CategoryCreateRequest request = new CategoryCreateRequest("Updated", "updated", "New desc", null);
        when(categoryRepository.existsBySlugAndIdNot("updated", 1L)).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.update(1L, request));
    }

    @Test
    @DisplayName("Should delete category")
    void shouldDeleteCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("Should throw when deleting non-existent category")
    void shouldThrowWhenDeletingNonExistent() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.delete(1L));
        verify(categoryRepository, never()).delete(any());
    }
}
