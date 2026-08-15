package com.tiagofsps.hunting_fishing_store.catalog.repository;

import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductItemRepository extends JpaRepository<ProductItem, Long> {
    Optional<ProductItem> findBySku(String sku);
}
