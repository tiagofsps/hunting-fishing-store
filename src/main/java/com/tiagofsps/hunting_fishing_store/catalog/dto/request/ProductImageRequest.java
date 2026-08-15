package com.tiagofsps.hunting_fishing_store.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductImageRequest(
        @NotBlank
        String imageUrl,

        String altText,

        Boolean isPrimary,

        @NotNull
        Integer displayOrder
) {
}
