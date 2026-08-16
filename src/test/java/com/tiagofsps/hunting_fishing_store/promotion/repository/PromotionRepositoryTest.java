package com.tiagofsps.hunting_fishing_store.promotion.repository;

import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import com.tiagofsps.hunting_fishing_store.promotion.entity.Promotion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
class PromotionRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private PromotionRepository promotionRepository;

    @Test
    void shouldFindActivePromotions() {
        Promotion p = new Promotion();
        p.setName("Active Sale");
        p.setDiscountType(DiscountType.PERCENTAGE);
        p.setDiscountValue(BigDecimal.valueOf(10));
        p.setValidFrom(LocalDateTime.now().minusDays(1));
        p.setValidTo(LocalDateTime.now().plusDays(1));
        p.setActive(true);
        promotionRepository.save(p);

        List<Promotion> active = promotionRepository.findActivePromotions(LocalDateTime.now());

        assertFalse(active.isEmpty());
        assertEquals("Active Sale", active.get(0).getName());
    }

    @Test
    void shouldFindActivePromotionsOrderedByPriority() {
        Promotion p1 = new Promotion();
        p1.setName("Low Priority");
        p1.setDiscountType(DiscountType.PERCENTAGE);
        p1.setDiscountValue(BigDecimal.valueOf(10));
        p1.setValidFrom(LocalDateTime.now().minusDays(1));
        p1.setValidTo(LocalDateTime.now().plusDays(1));
        p1.setPriority(1);
        promotionRepository.save(p1);

        Promotion p2 = new Promotion();
        p2.setName("High Priority");
        p2.setDiscountType(DiscountType.PERCENTAGE);
        p2.setDiscountValue(BigDecimal.valueOf(20));
        p2.setValidFrom(LocalDateTime.now().minusDays(1));
        p2.setValidTo(LocalDateTime.now().plusDays(1));
        p2.setPriority(10);
        promotionRepository.save(p2);

        List<Promotion> active = promotionRepository.findActivePromotions(LocalDateTime.now());

        assertEquals(2, active.size());
        assertEquals("High Priority", active.get(0).getName());
    }
}
