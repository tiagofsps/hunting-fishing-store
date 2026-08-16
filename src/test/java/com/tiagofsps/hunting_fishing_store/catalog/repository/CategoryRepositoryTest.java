package com.tiagofsps.hunting_fishing_store.catalog.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.Optional;

import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;

@Testcontainers
@DataJpaTest
class CategoryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSaveCategory() {
        Category category = new Category();
        category.setName("Fishing");
        category.setSlug("fishing");

        Category saved = categoryRepository.save(category);

        assertNotNull(saved.getId());
    }

    @Test
    void shouldFindCategoryBySlug() {
        Category category = new Category();
        category.setName("Fishing");
        category.setSlug("fishing");
        categoryRepository.save(category);

        Optional<Category> result = categoryRepository.findBySlug("fishing");

        assertTrue(result.isPresent());
        assertEquals("Fishing", result.get().getName());
    }

    @Test
    void shouldCheckIfSlugExists() {
        Category category = new Category();
        category.setName("Fishing");
        category.setSlug("fishing");
        categoryRepository.save(category);

        assertTrue(categoryRepository.existsBySlug("fishing"));
        assertFalse(categoryRepository.existsBySlug("hunting"));
    }

    @Test
    void shouldCheckIfSlugExistsAndIdNot() {
        Category category = new Category();
        category.setName("Fishing");
        category.setSlug("fishing");
        Category saved = categoryRepository.save(category);

        assertFalse(categoryRepository.existsBySlugAndIdNot("fishing", saved.getId()));
        
        Category other = new Category();
        other.setName("Other");
        other.setSlug("other");
        categoryRepository.save(other);
        
        assertTrue(categoryRepository.existsBySlugAndIdNot("other", saved.getId()));
    }
}
