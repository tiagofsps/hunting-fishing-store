package com.tiagofsps.hunting_fishing_store.coupon.service;

import com.tiagofsps.hunting_fishing_store.common.exception.CouponInvalidException;
import com.tiagofsps.hunting_fishing_store.common.exception.CouponNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.CouponUsageLimitReachedException;
import com.tiagofsps.hunting_fishing_store.coupon.dto.request.CouponRequest;
import com.tiagofsps.hunting_fishing_store.coupon.dto.response.CouponResponse;
import com.tiagofsps.hunting_fishing_store.coupon.entity.Coupon;
import com.tiagofsps.hunting_fishing_store.coupon.entity.CouponUsage;
import com.tiagofsps.hunting_fishing_store.coupon.repository.CouponRepository;
import com.tiagofsps.hunting_fishing_store.coupon.repository.CouponUsageRepository;
import com.tiagofsps.hunting_fishing_store.order.entity.Order;
import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    public CouponService(CouponRepository couponRepository, CouponUsageRepository couponUsageRepository) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
    }

    private CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getUsageLimit(),
                coupon.getUsageLimitPerUser(),
                coupon.getValidFrom(),
                coupon.getValidTo(),
                coupon.getActive()
        );
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {
        Coupon coupon = new Coupon();
        coupon.setCode(request.code().toUpperCase());
        coupon.setDiscountType(request.discountType());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinOrderAmount(request.minOrderAmount());
        coupon.setUsageLimit(request.usageLimit());
        coupon.setUsageLimitPerUser(request.usageLimitPerUser());
        coupon.setValidFrom(request.validFrom());
        coupon.setValidTo(request.validTo());

        return toResponse(couponRepository.save(coupon));
    }

    public List<CouponResponse> findAll() {
        return couponRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CouponResponse findById(Long id) {
        return couponRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new CouponNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Coupon validateAndGet(String code, Long userId, BigDecimal orderTotal) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new CouponNotFoundException(code));

        if (!coupon.getActive()) {
            throw new CouponInvalidException("Coupon is not active");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidTo())) {
            throw new CouponInvalidException("Coupon is expired or not yet valid");
        }

        if (coupon.getMinOrderAmount() != null && orderTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new CouponInvalidException("Order total is below the minimum required for this coupon");
        }

        if (coupon.getUsageLimit() != null) {
            long totalUsage = couponUsageRepository.countByCouponId(coupon.getId());
            if (totalUsage >= coupon.getUsageLimit()) {
                throw new CouponUsageLimitReachedException(code);
            }
        }

        if (coupon.getUsageLimitPerUser() != null) {
            long userUsage = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
            if (userUsage >= coupon.getUsageLimitPerUser()) {
                throw new CouponInvalidException("You have already reached the usage limit for this coupon");
            }
        }

        return coupon;
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderTotal) {
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            return orderTotal.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            return coupon.getDiscountValue().min(orderTotal);
        }
    }

    @Transactional
    public void recordUsage(Coupon coupon, User user, Order order) {
        CouponUsage usage = new CouponUsage();
        usage.setCoupon(coupon);
        usage.setUser(user);
        usage.setOrder(order);
        usage.setUsedAt(LocalDateTime.now());
        couponUsageRepository.save(usage);
    }
}
