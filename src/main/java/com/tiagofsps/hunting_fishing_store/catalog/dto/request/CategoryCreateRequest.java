package com.tiagofsps.hunting_fishing_store.catalog.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CategoryCreateRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 100)
        String slug,

        @Size(max = 500)
        String description,

        @Valid
        Map<String, CategoryTranslationRequest> translations,

        Long parentCategoryId
) {
}
