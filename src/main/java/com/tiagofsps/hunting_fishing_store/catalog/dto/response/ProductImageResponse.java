package com.tiagofsps.hunting_fishing_store.catalog.dto.response;

public record ProductImageResponse(
        Long id,
        String imageUrl,
        String altText,
        Boolean isPrimary,
        Integer displayOrder
) {
}
