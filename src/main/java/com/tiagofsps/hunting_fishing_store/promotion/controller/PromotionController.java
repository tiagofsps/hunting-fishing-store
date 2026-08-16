package com.tiagofsps.hunting_fishing_store.promotion.controller;

import com.tiagofsps.hunting_fishing_store.promotion.dto.request.PromotionRequest;
import com.tiagofsps.hunting_fishing_store.promotion.dto.response.PromotionResponse;
import com.tiagofsps.hunting_fishing_store.promotion.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionResponse create(@Valid @RequestBody PromotionRequest request) {
        return promotionService.create(request);
    }

    @GetMapping
    public List<PromotionResponse> findAll() {
        return promotionService.findAll();
    }

    @GetMapping("/{id}")
    public PromotionResponse findById(@PathVariable Long id) {
        return promotionService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        promotionService.delete(id);
    }
}
