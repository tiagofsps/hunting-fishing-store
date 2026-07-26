package com.tiagofsps.hunting_fishing_store.catalog.dto.request;

public record BrandCreateRequest(
        String name,
        String slug,
        String logoUrl
) {
}
