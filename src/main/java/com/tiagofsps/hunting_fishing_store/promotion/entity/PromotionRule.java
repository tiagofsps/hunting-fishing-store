package com.tiagofsps.hunting_fishing_store.promotion.entity;

import com.tiagofsps.hunting_fishing_store.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "promotion_rules")
public class PromotionRule extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;
}
