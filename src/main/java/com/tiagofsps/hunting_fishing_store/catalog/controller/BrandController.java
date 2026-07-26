package com.tiagofsps.hunting_fishing_store.catalog.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.service.BrandService;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public List<Brand> findAll() {
        return brandService.findAll();
    }
}
