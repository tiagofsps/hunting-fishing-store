package com.tiagofsps.hunting_fishing_store.catalog.repository;

import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
