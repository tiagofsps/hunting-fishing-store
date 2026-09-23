package com.tiagofsps.hunting_fishing_store.catalog.controller;

import java.util.List;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.BrandCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.BrandResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.tiagofsps.hunting_fishing_store.catalog.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;


@RestController
@RequestMapping("/api/v1/brands")
@Tag(name = "Brands", description = "Brand catalog management")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    @Operation(summary = "List all brands")
    public List<BrandResponse> findAll() {
        return brandService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single brand")
    public BrandResponse findById(@PathVariable Long id) {
        return brandService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Create a new brand")
    public BrandResponse create(@Valid   @RequestBody BrandCreateRequest request) {
        return brandService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Delete a brand")
    public void delete(@PathVariable Long id) {
        brandService.delete(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Update a brand")
    public BrandResponse update(@PathVariable Long id, @Valid @RequestBody BrandCreateRequest request) {
        BrandResponse entity = brandService.update(id, request);        
        return entity;
    }
    
}
