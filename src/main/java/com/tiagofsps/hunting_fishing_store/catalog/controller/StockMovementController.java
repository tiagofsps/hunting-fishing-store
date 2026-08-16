package com.tiagofsps.hunting_fishing_store.catalog.controller;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.StockMovementRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.StockMovementResponse;
import com.tiagofsps.hunting_fishing_store.catalog.service.StockMovementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse recordMovement(@Valid @RequestBody StockMovementRequest request) {
        return stockMovementService.recordMovement(request);
    }

    @GetMapping("/item/{productItemId}")
    public List<StockMovementResponse> getMovementsByItem(@PathVariable Long productItemId) {
        return stockMovementService.getMovementsByProductItem(productItemId);
    }
}
