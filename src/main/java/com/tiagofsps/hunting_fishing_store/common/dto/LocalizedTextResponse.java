package com.tiagofsps.hunting_fishing_store.common.dto;

/**
 * A single localized content value returned by the API. `locale` is carried by
 * the map key in the owning response, so it is not repeated here.
 */
public record LocalizedTextResponse(
        String name,
        String description
) {
}
