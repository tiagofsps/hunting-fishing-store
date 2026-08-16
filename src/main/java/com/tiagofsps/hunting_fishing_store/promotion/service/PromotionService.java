package com.tiagofsps.hunting_fishing_store.promotion.service;

import com.tiagofsps.hunting_fishing_store.common.exception.PromotionNotFoundException;
import com.tiagofsps.hunting_fishing_store.promotion.dto.request.PromotionRequest;
import com.tiagofsps.hunting_fishing_store.promotion.dto.response.PromotionResponse;
import com.tiagofsps.hunting_fishing_store.promotion.dto.response.PromotionRuleResponse;
import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import com.tiagofsps.hunting_fishing_store.promotion.entity.Promotion;
import com.tiagofsps.hunting_fishing_store.promotion.entity.PromotionRule;
import com.tiagofsps.hunting_fishing_store.promotion.entity.PromotionTargetType;
import com.tiagofsps.hunting_fishing_store.promotion.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    private PromotionResponse toResponse(Promotion promotion) {
        List<PromotionRuleResponse> rules = promotion.getRules().stream()
                .map(rule -> new PromotionRuleResponse(rule.getId(), rule.getTargetType(), rule.getTargetId()))
                .toList();

        return new PromotionResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getDiscountType(),
                promotion.getDiscountValue(),
                promotion.getPriority(),
                promotion.getStackable(),
                promotion.getValidFrom(),
                promotion.getValidTo(),
                promotion.getActive(),
                rules
        );
    }

    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        Promotion promotion = new Promotion();
        promotion.setName(request.name());
        promotion.setDiscountType(request.discountType());
        promotion.setDiscountValue(request.discountValue());
        promotion.setPriority(request.priority() != null ? request.priority() : 0);
        promotion.setStackable(request.stackable() != null ? request.stackable() : true);
        promotion.setValidFrom(request.validFrom());
        promotion.setValidTo(request.validTo());

        if (request.rules() != null) {
            request.rules().forEach(ruleReq -> {
                PromotionRule rule = new PromotionRule();
                rule.setTargetType(ruleReq.targetType());
                rule.setTargetId(ruleReq.targetId());
                rule.setPromotion(promotion);
                promotion.getRules().add(rule);
            });
        }

        Promotion saved = promotionRepository.save(promotion);
        return toResponse(saved);
    }

    public List<PromotionResponse> findAll() {
        return promotionRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PromotionResponse findById(Long id) {
        return promotionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new PromotionNotFoundException(id));
    }

    public BigDecimal calculateDiscountedPrice(BigDecimal basePrice, Long productId, Long brandId, Long categoryId) {
        List<Promotion> activePromotions = promotionRepository.findActivePromotions(LocalDateTime.now());
        
        BigDecimal currentPrice = basePrice;
        
        for (Promotion promotion : activePromotions) {
            boolean applies = promotion.getRules().stream().anyMatch(rule -> 
                (rule.getTargetType() == PromotionTargetType.PRODUCT && rule.getTargetId().equals(productId)) ||
                (rule.getTargetType() == PromotionTargetType.BRAND && rule.getTargetId().equals(brandId)) ||
                (rule.getTargetType() == PromotionTargetType.CATEGORY && rule.getTargetId().equals(categoryId))
            );

            if (applies) {
                if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
                    BigDecimal discount = currentPrice.multiply(promotion.getDiscountValue())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    currentPrice = currentPrice.subtract(discount);
                } else {
                    currentPrice = currentPrice.subtract(promotion.getDiscountValue());
                }

                if (!promotion.getStackable()) {
                    break; // Stop applying more promotions
                }
            }
        }

        return currentPrice.max(BigDecimal.ZERO);
    }

    @Transactional
    public void delete(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new PromotionNotFoundException(id);
        }
        promotionRepository.deleteById(id);
    }
}
