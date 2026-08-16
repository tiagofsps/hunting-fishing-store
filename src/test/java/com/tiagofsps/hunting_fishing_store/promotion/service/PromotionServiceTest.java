package com.tiagofsps.hunting_fishing_store.promotion.service;

import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import com.tiagofsps.hunting_fishing_store.promotion.entity.Promotion;
import com.tiagofsps.hunting_fishing_store.promotion.entity.PromotionRule;
import com.tiagofsps.hunting_fishing_store.promotion.entity.PromotionTargetType;
import com.tiagofsps.hunting_fishing_store.promotion.repository.PromotionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionService promotionService;

    @Test
    @DisplayName("Should apply percentage discount")
    void shouldApplyPercentageDiscount() {
        Promotion promotion = new Promotion();
        promotion.setDiscountType(DiscountType.PERCENTAGE);
        promotion.setDiscountValue(BigDecimal.valueOf(20.0));
        promotion.setPriority(1);
        promotion.setStackable(true);

        PromotionRule rule = new PromotionRule();
        rule.setTargetType(PromotionTargetType.BRAND);
        rule.setTargetId(1L);
        promotion.setRules(List.of(rule));

        when(promotionRepository.findActivePromotions(any())).thenReturn(List.of(promotion));

        BigDecimal result = promotionService.calculateDiscountedPrice(BigDecimal.valueOf(100.0), 1L, 1L, 1L);

        assertEquals(0, result.compareTo(BigDecimal.valueOf(80.0)));
    }

    @Test
    @DisplayName("Should apply fixed amount discount")
    void shouldApplyFixedAmountDiscount() {
        Promotion promotion = new Promotion();
        promotion.setDiscountType(DiscountType.FIXED_AMOUNT);
        promotion.setDiscountValue(BigDecimal.valueOf(15.0));
        promotion.setPriority(1);
        promotion.setStackable(true);

        PromotionRule rule = new PromotionRule();
        rule.setTargetType(PromotionTargetType.PRODUCT);
        rule.setTargetId(1L);
        promotion.setRules(List.of(rule));

        when(promotionRepository.findActivePromotions(any())).thenReturn(List.of(promotion));

        BigDecimal result = promotionService.calculateDiscountedPrice(BigDecimal.valueOf(100.0), 1L, 1L, 1L);

        assertEquals(0, result.compareTo(BigDecimal.valueOf(85.0)));
    }

    @Test
    @DisplayName("Should handle non-stackable promotions")
    void shouldHandleNonStackable() {
        Promotion p1 = new Promotion();
        p1.setDiscountType(DiscountType.PERCENTAGE);
        p1.setDiscountValue(BigDecimal.valueOf(10.0));
        p1.setPriority(10);
        p1.setStackable(false);
        PromotionRule r1 = new PromotionRule();
        r1.setTargetType(PromotionTargetType.PRODUCT);
        r1.setTargetId(1L);
        p1.setRules(List.of(r1));

        Promotion p2 = new Promotion();
        p2.setDiscountType(DiscountType.FIXED_AMOUNT);
        p2.setDiscountValue(BigDecimal.valueOf(5.0));
        p2.setPriority(5);
        p2.setStackable(true);
        PromotionRule r2 = new PromotionRule();
        r2.setTargetType(PromotionTargetType.PRODUCT);
        r2.setTargetId(1L);
        p2.setRules(List.of(r2));

        when(promotionRepository.findActivePromotions(any())).thenReturn(List.of(p1, p2));

        BigDecimal result = promotionService.calculateDiscountedPrice(BigDecimal.valueOf(100.0), 1L, 1L, 1L);

        // Only p1 should apply because it's not stackable
        assertEquals(0, result.compareTo(BigDecimal.valueOf(90.0)));
    }
}
