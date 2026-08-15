package com.tiagofsps.hunting_fishing_store.catalog.dto.response;

public record CategoryResponse(
    Long id,
    String name,
    String slug,
    String description,
    Long parentCategoryId
) {}
