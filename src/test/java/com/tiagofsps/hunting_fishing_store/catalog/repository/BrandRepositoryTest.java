package com.tiagofsps.hunting_fishing_store.catalog.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tiagofsps.hunting_fishing_store.BaseRepositoryTest;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

public class BrandRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private BrandRepository brandRepository;

    @Test
    void shouldSaveBrand() {

        Brand brand = new Brand();

        brand.setName("Shimano");
        brand.setSlug("shimano");
        brand.setLogoUrl("logo.png");

        Brand saved = brandRepository.save(brand);

        assertNotNull(saved.getId());
    }

    @Test
    void shouldFindBrandBySlug() {

        Brand brand = new Brand();
        brand.setName("Shimano");
        brand.setSlug("shimano");
        brand.setLogoUrl("logo.png");

        brandRepository.save(brand);

        Optional<Brand> result =
                brandRepository.findBySlug("shimano");

        assertTrue(result.isPresent());
        assertEquals("Shimano", result.get().getName());
        assertEquals("shimano", result.get().getSlug());
        assertEquals("logo.png", result.get().getLogoUrl());
    }

    @Test
    void shouldReturnEmptyWhenSlugDoesNotExist() {

        Optional<Brand> result =
                brandRepository.findBySlug("does-not-exist");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenSlugExists() {

        Brand brand = new Brand();
        brand.setName("Shimano");
        brand.setSlug("shimano");
        brand.setLogoUrl("logo.png");

        brandRepository.save(brand);

        assertTrue(
                brandRepository.existsBySlug("shimano"));
    }

    @Test
    void shouldReturnFalseWhenSlugDoesNotExist() {

        assertFalse(
                brandRepository.existsBySlug("does-not-exist"));
    }

    @Test
    void shouldReturnFalseWhenSlugBelongsToSameBrand() {

        Brand brand = new Brand();
        brand.setName("Shimano");
        brand.setSlug("shimano");
        brand.setLogoUrl("logo.png");

        Brand saved = brandRepository.save(brand);

        assertFalse(
                brandRepository.existsBySlugAndIdNot(
                        "shimano",
                        saved.getId()));
    }

    @Test
    void shouldReturnTrueWhenSlugBelongsToAnotherBrand() {

        Brand shimano = new Brand();
        shimano.setName("Shimano");
        shimano.setSlug("shimano");
        shimano.setLogoUrl("shimano.png");

        Brand daiwa = new Brand();
        daiwa.setName("Daiwa");
        daiwa.setSlug("daiwa");
        daiwa.setLogoUrl("daiwa.png");

        Brand savedShimano = brandRepository.save(shimano);
        Brand savedDaiwa = brandRepository.save(daiwa);

        assertTrue(
                brandRepository.existsBySlugAndIdNot(
                        savedShimano.getSlug(),
                        savedDaiwa.getId()));
    }

    @Test
    void shouldRejectDuplicateSlug() {
        Brand first = new Brand();
        first.setName("Shimano");
        first.setSlug("shimano");
        first.setLogoUrl("shimano.png");

        brandRepository.saveAndFlush(first);

        Brand duplicate = new Brand();
        duplicate.setName("Shimano Duplicate");
        duplicate.setSlug("shimano");
        duplicate.setLogoUrl("duplicate.png");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> brandRepository.saveAndFlush(duplicate)
        );
    }

    @Test
    void shouldRejectNullName() {
        Brand brand = new Brand();

        brand.setName(null);
        brand.setSlug("shimano");
        brand.setLogoUrl("logo.png");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> brandRepository.saveAndFlush(brand)
        );
    }

    @Test
    void shouldRejectNullSlug() {
        Brand brand = new Brand();

        brand.setName("Shimano");
        brand.setSlug(null);
        brand.setLogoUrl("logo.png");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> brandRepository.saveAndFlush(brand)
        );
    }

}
