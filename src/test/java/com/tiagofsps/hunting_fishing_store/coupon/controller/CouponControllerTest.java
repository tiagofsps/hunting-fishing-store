package com.tiagofsps.hunting_fishing_store.coupon.controller;

import com.tiagofsps.hunting_fishing_store.BaseControllerTest;
import com.tiagofsps.hunting_fishing_store.coupon.dto.request.CouponRequest;
import com.tiagofsps.hunting_fishing_store.coupon.dto.response.CouponResponse;
import com.tiagofsps.hunting_fishing_store.coupon.service.CouponService;
import com.tiagofsps.hunting_fishing_store.common.exception.CouponNotFoundException;
import com.tiagofsps.hunting_fishing_store.promotion.entity.DiscountType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CouponControllerTest extends BaseControllerTest {

    @MockitoBean
    private CouponService couponService;

    @Test
    void shouldCreateCoupon() throws Exception {
        CouponResponse response = new CouponResponse(1L, "SAVE20", DiscountType.PERCENTAGE, BigDecimal.valueOf(20), null, null, null, LocalDateTime.now(), LocalDateTime.now().plusDays(1), true);
        when(couponService.create(any(CouponRequest.class))).thenReturn(response);

        String request = """
                {
                    "code": "SAVE20",
                    "discountType": "PERCENTAGE",
                    "discountValue": 20,
                    "validFrom": "2026-08-15T00:00:00",
                    "validTo": "2026-08-20T00:00:00"
                }
                """;

        mockMvc.perform(post("/api/v1/coupons").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SAVE20"));
    }

    @Test
    void shouldReturnAllCoupons() throws Exception {
        CouponResponse response = new CouponResponse(1L, "SAVE20", DiscountType.PERCENTAGE, BigDecimal.valueOf(20), null, null, null, LocalDateTime.now(), LocalDateTime.now().plusDays(1), true);
        when(couponService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/coupons").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SAVE20"));
    }

    @Test
    void shouldReturnNotFoundWhenCouponDoesNotExist() throws Exception {
        when(couponService.findById(1L)).thenThrow(new CouponNotFoundException(1L));

        mockMvc.perform(get("/api/v1/coupons/1").with(adminJwt()))
                .andExpect(status().isNotFound());
    }
}
