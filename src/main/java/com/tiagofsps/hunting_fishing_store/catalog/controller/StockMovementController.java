package com.tiagofsps.hunting_fishing_store.catalog.controller;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.StockMovementRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.StockMovementResponse;
import com.tiagofsps.hunting_fishing_store.catalog.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-movements")
@Tag(name = "Stock Movements", description = "Inventory movement tracking")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Record a stock movement")
    public StockMovementResponse recordMovement(@Valid @RequestBody StockMovementRequest request) {
        return stockMovementService.recordMovement(request);
    }

    @GetMapping("/item/{productItemId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] List movements for a product item")
    public List<StockMovementResponse> getMovementsByItem(@PathVariable Long productItemId) {
        return stockMovementService.getMovementsByProductItem(productItemId);
    }
}
