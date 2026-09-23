package com.tiagofsps.hunting_fishing_store.catalog.dto.response;

import com.tiagofsps.hunting_fishing_store.catalog.entity.LicenseType;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        Boolean requiresLicense,
        LicenseType licenseType,
        Long categoryId,
        Long brandId,
        List<ProductItemResponse> items,
        List<ProductImageResponse> images
) {
}
