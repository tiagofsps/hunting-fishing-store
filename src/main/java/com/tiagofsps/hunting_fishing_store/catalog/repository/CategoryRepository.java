package com.tiagofsps.hunting_fishing_store.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findBySlug(String slug);

  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, Long id);
}
