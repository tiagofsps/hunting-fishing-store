package com.tiagofsps.hunting_fishing_store.catalog.dto.response;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        Boolean requiresLicense,
        Long categoryId,
        Long brandId,
        List<ProductItemResponse> items,
        List<ProductImageResponse> images
) {
}
