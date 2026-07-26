package com.tiagofsps.hunting_fishing_store.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.BrandCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.BrandResponse;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<BrandResponse> findAll() {
        return brandRepository.findAll().stream().map(this::toResponse).toList();
    }

    private BrandResponse toResponse(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getName(), brand.getSlug(), brand.getLogoUrl());
    }

    public BrandResponse create(BrandCreateRequest request) {
        if(brandRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("Slug already exists");
        }

        Brand brand = new Brand();
        brand.setName(request.name());
        brand.setSlug(request.slug());
        brand.setLogoUrl(request.logoUrl());

        Brand savedBrand = brandRepository.save(brand);

        return new BrandResponse(savedBrand.getId(), savedBrand.getName(), savedBrand.getSlug(), savedBrand.getLogoUrl());
    } 

}
