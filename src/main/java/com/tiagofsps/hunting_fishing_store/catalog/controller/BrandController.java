package com.tiagofsps.hunting_fishing_store.catalog.controller;

import java.util.List;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.BrandCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.BrandResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.tiagofsps.hunting_fishing_store.catalog.service.BrandService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public List<BrandResponse> findAll() {
        return brandService.findAll();
    }

    @GetMapping("/{id}")
    public BrandResponse findById(@PathVariable Long id) {
        return brandService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BrandResponse create(@Valid   @RequestBody BrandCreateRequest request) {
        return brandService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        brandService.delete(id);
    }

    @PutMapping("/{id}")
    public BrandResponse update(@PathVariable Long id, @Valid @RequestBody BrandCreateRequest request) {
        BrandResponse entity = brandService.update(id, request);        
        return entity;
    }
    
}
