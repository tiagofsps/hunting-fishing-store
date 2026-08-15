package com.tiagofsps.hunting_fishing_store.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

  Optional<Product> findBySlug(String slug);

  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, Long id);
}
