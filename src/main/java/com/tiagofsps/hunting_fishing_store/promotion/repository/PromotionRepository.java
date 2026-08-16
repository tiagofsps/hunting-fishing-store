package com.tiagofsps.hunting_fishing_store.promotion.repository;

import com.tiagofsps.hunting_fishing_store.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p WHERE p.active = true AND p.validFrom <= :now AND p.validTo >= :now ORDER BY p.priority DESC")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
}
