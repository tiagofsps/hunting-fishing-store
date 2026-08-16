package com.tiagofsps.hunting_fishing_store.catalog.repository;

import com.tiagofsps.hunting_fishing_store.catalog.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductItemId(Long productItemId);
}
