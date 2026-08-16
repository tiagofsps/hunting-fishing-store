package com.tiagofsps.hunting_fishing_store.promotion.service;

import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import com.tiagofsps.hunting_fishing_store.promotion.entity.Promotion;
import com.tiagofsps.hunting_fishing_store.promotion.entity.PromotionRule;
import com.tiagofsps.hunting_fishing_store.promotion.entity.PromotionTargetType;
import com.tiagofsps.hunting_fishing_store.promotion.repository.PromotionRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.PromotionNotFoundException;
import com.tiagofsps.hunting_fishing_store.promotion.dto.request.PromotionRequest;
import com.tiagofsps.hunting_fishing_store.promotion.dto.request.PromotionRuleRequest;
import com.tiagofsps.hunting_fishing_store.promotion.dto.response.PromotionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("Should cap discount at zero")
    void shouldCapDiscountAtZero() {
        Promotion p = new Promotion();
        p.setDiscountType(DiscountType.FIXED_AMOUNT);
        p.setDiscountValue(BigDecimal.valueOf(200.0));
        PromotionRule r = new PromotionRule();
        r.setTargetType(PromotionTargetType.PRODUCT);
        r.setTargetId(1L);
        p.setRules(List.of(r));

        when(promotionRepository.findActivePromotions(any())).thenReturn(List.of(p));

        BigDecimal result = promotionService.calculateDiscountedPrice(BigDecimal.valueOf(100.0), 1L, 1L, 1L);

        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should apply category-based promotion")
    void shouldApplyCategoryPromotion() {
        Promotion p = new Promotion();
        p.setDiscountType(DiscountType.FIXED_AMOUNT);
        p.setDiscountValue(BigDecimal.TEN);
        PromotionRule r = new PromotionRule();
        r.setTargetType(PromotionTargetType.CATEGORY);
        r.setTargetId(5L);
        p.setRules(List.of(r));

        when(promotionRepository.findActivePromotions(any())).thenReturn(List.of(p));

        BigDecimal result = promotionService.calculateDiscountedPrice(BigDecimal.valueOf(100.0), 1L, 1L, 5L);

        assertEquals(0, result.compareTo(BigDecimal.valueOf(90.0)));
    }

    @Test
    @DisplayName("Should not apply promotion if no rules match")
    void shouldNotApplyIfNoMatch() {
        Promotion p = new Promotion();
        p.setDiscountType(DiscountType.FIXED_AMOUNT);
        p.setDiscountValue(BigDecimal.TEN);
        PromotionRule r = new PromotionRule();
        r.setTargetType(PromotionTargetType.PRODUCT);
        r.setTargetId(99L);
        p.setRules(List.of(r));

        when(promotionRepository.findActivePromotions(any())).thenReturn(List.of(p));

        BigDecimal result = promotionService.calculateDiscountedPrice(BigDecimal.valueOf(100.0), 1L, 1L, 1L);

        assertEquals(0, result.compareTo(BigDecimal.valueOf(100.0)));
    }

    @Test
    @DisplayName("Should create promotion with rules")
    void shouldCreatePromotionWithRules() {
        PromotionRuleRequest ruleReq = new PromotionRuleRequest(PromotionTargetType.BRAND, 1L);
        PromotionRequest request = new PromotionRequest("Sale", DiscountType.PERCENTAGE, BigDecimal.valueOf(10), 1, true, LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of(ruleReq));
        
        when(promotionRepository.save(any())).thenAnswer(i -> {
            Promotion p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        PromotionResponse response = promotionService.create(request);

        assertEquals("Sale", response.name());
        assertEquals(1, response.rules().size());
        verify(promotionRepository).save(any());
    }

    @Test
    @DisplayName("Should return all promotions")
    void shouldReturnAllPromotions() {
        Promotion p1 = new Promotion(); p1.setId(1L); p1.setRules(new ArrayList<>());
        Promotion p2 = new Promotion(); p2.setId(2L); p2.setRules(new ArrayList<>());
        when(promotionRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PromotionResponse> result = promotionService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should throw when promotion to delete not found")
    void shouldThrowWhenDeleteNotFound() {
        when(promotionRepository.existsById(1L)).thenReturn(false);

        assertThrows(PromotionNotFoundException.class, () -> promotionService.delete(1L));
    }

    @Test
    @DisplayName("Should find promotion by id")
    void shouldFindById() {
        Promotion p = new Promotion();
        p.setId(1L);
        p.setName("Sale");
        p.setRules(new ArrayList<>());
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(p));

        PromotionResponse response = promotionService.findById(1L);

        assertEquals(1L, response.id());
    }

    @Test
    @DisplayName("Should throw when promotion not found")
    void shouldThrowWhenNotFound() {
        when(promotionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PromotionNotFoundException.class, () -> promotionService.findById(1L));
    }

    @Test
    @DisplayName("Should delete promotion")
    void shouldDelete() {
        when(promotionRepository.existsById(1L)).thenReturn(true);

        promotionService.delete(1L);

        verify(promotionRepository).deleteById(1L);
    }
}
