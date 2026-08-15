package com.tiagofsps.hunting_fishing_store.catalog.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
