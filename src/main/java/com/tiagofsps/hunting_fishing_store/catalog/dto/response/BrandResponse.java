package com.tiagofsps.hunting_fishing_store.catalog.dto.response;

public record BrandResponse(
    Long id,
    String name,
    String slug,
    String logoUrl
) {}
