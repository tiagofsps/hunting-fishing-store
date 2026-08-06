package com.tiagofsps.hunting_fishing_store.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {

  Optional<Brand> findById(Long id);
  
  Optional<Brand> findBySlug(String slug);

  boolean existsBySlug(String slug);
}
