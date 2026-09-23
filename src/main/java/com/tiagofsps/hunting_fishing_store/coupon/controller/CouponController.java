package com.tiagofsps.hunting_fishing_store.coupon.controller;

import com.tiagofsps.hunting_fishing_store.coupon.dto.request.CouponRequest;
import com.tiagofsps.hunting_fishing_store.coupon.dto.response.CouponResponse;
import com.tiagofsps.hunting_fishing_store.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupons", description = "Coupon management (all ADMIN)")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Create a new coupon")
    public CouponResponse create(@Valid @RequestBody CouponRequest request) {
        return couponService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] List all coupons")
    public List<CouponResponse> findAll() {
        return couponService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Get a coupon")
    public CouponResponse findById(@PathVariable Long id) {
        return couponService.findById(id);
    }
}
