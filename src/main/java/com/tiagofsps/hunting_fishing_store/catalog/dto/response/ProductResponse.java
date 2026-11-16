package com.tiagofsps.hunting_fishing_store.catalog.dto.response;

import com.tiagofsps.hunting_fishing_store.catalog.entity.LicenseType;
import com.tiagofsps.hunting_fishing_store.common.dto.LocalizedTextResponse;

import java.util.List;
import java.util.Map;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        Map<String, LocalizedTextResponse> translations,
        Boolean requiresLicense,
        LicenseType licenseType,
        Long categoryId,
        Long brandId,
        List<ProductItemResponse> items,
        List<ProductImageResponse> images
) {
}
