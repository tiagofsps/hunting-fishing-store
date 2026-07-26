package com.tiagofsps.hunting_fishing_store.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<Brand> findAll() {
        return brandRepository.findAll();
    }
}
