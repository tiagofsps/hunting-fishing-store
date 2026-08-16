package com.tiagofsps.hunting_fishing_store.coupon.entity;

import com.tiagofsps.hunting_fishing_store.common.BaseEntity;
import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "coupons")
public class Coupon extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 19, scale = 2)
    private BigDecimal minOrderAmount;

    private Integer usageLimit;

    private Integer usageLimitPerUser;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validTo;

    @Column(nullable = false)
    private Boolean active = true;
}
