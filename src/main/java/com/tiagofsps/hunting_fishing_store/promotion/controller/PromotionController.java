package com.tiagofsps.hunting_fishing_store.promotion.controller;

import com.tiagofsps.hunting_fishing_store.promotion.dto.request.PromotionRequest;
import com.tiagofsps.hunting_fishing_store.promotion.dto.response.PromotionResponse;
import com.tiagofsps.hunting_fishing_store.promotion.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@Tag(name = "Promotions", description = "Promotion and discount campaign management")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Create a new promotion")
    public PromotionResponse create(@Valid @RequestBody PromotionRequest request) {
        return promotionService.create(request);
    }

    @GetMapping
    @Operation(summary = "List all active promotions")
    public List<PromotionResponse> findAll() {
        return promotionService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single promotion")
    public PromotionResponse findById(@PathVariable Long id) {
        return promotionService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Delete a promotion")
    public void delete(@PathVariable Long id) {
        promotionService.delete(id);
    }
}
