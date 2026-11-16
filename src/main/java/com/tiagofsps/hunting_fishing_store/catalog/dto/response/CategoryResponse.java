package com.tiagofsps.hunting_fishing_store.catalog.dto.response;

import com.tiagofsps.hunting_fishing_store.common.dto.LocalizedTextResponse;

import java.util.Map;

public record CategoryResponse(
    Long id,
    String name,
    String slug,
    String description,
    Map<String, LocalizedTextResponse> translations,
    Long parentCategoryId,
    Long productCount
) {}
