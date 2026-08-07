package com.tiagofsps.hunting_fishing_store.catalog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.BrandCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.BrandResponse;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.BrandAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.BrandNotFoundException;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    private Brand brand;

    @BeforeEach
    void setup() {
        brand = new Brand();
        brand.setId(1L);
        brand.setName("Shimano");
        brand.setSlug("shimano");
        brand.setLogoUrl("logo.png");
    }

    @Test
    @DisplayName("Should return brand when id exists")
    void shouldFindBrandById() {

        when(brandRepository.findById(1L))
                .thenReturn(Optional.of(brand));

        BrandResponse response = brandService.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("Shimano", response.name());
        assertEquals("shimano", response.slug());

        verify(brandRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when brand does not exist")
    void shouldThrowWhenBrandDoesNotExist() {

        when(brandRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                BrandNotFoundException.class,
                () -> brandService.findById(1L));

        verify(brandRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return all brands")
    void shouldReturnAllBrands() {

        when(brandRepository.findAll())
                .thenReturn(List.of(brand));

        List<BrandResponse> result = brandService.findAll();

        assertEquals(1, result.size());
        assertEquals("Shimano", result.getFirst().name());

        verify(brandRepository).findAll();
    }

    @Test
    @DisplayName("Should create brand")
    void shouldCreateBrand() {

        BrandCreateRequest request =
                new BrandCreateRequest(
                        "Shimano",
                        "shimano",
                        "logo.png");

        when(brandRepository.existsBySlug("shimano"))
                .thenReturn(false);

        when(brandRepository.save(any(Brand.class)))
                .thenReturn(brand);

        BrandResponse response = brandService.create(request);

        assertEquals("Shimano", response.name());
        assertEquals("shimano", response.slug());

        verify(brandRepository).existsBySlug("shimano");
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    @DisplayName("Should not create duplicated slug")
    void shouldThrowWhenCreatingDuplicatedSlug() {

        BrandCreateRequest request =
                new BrandCreateRequest(
                        "Shimano",
                        "shimano",
                        "logo.png");

        when(brandRepository.existsBySlug("shimano"))
                .thenReturn(true);

        assertThrows(
                BrandAlreadyExistsException.class,
                () -> brandService.create(request));

        verify(brandRepository).existsBySlug("shimano");
        verify(brandRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update existing brand")
    void shouldUpdateBrand() {

        BrandCreateRequest request =
                new BrandCreateRequest(
                        "New Shimano",
                        "new-shimano",
                        "new-logo.png");

        when(brandRepository.existsBySlugAndIdNot("new-shimano", 1L))
                .thenReturn(false);

        when(brandRepository.findById(1L))
                .thenReturn(Optional.of(brand));

        when(brandRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BrandResponse response = brandService.update(1L, request);

        assertEquals("New Shimano", response.name());
        assertEquals("new-shimano", response.slug());

        verify(brandRepository).save(any());
    }

    @Test
    @DisplayName("Should not update duplicated slug")
    void shouldThrowWhenUpdatingDuplicatedSlug() {

        BrandCreateRequest request =
                new BrandCreateRequest(
                        "Shimano",
                        "shimano",
                        "logo.png");

        when(brandRepository.existsBySlugAndIdNot("shimano", 1L))
                .thenReturn(true);

        assertThrows(
                BrandAlreadyExistsException.class,
                () -> brandService.update(1L, request));

        verify(brandRepository, never()).findById(any());
        verify(brandRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when updating unexisting id")
    void shouldThrowWhenUpdatingUnexistingId() {
        
        BrandCreateRequest request =
                new BrandCreateRequest(
                        "Shimano",
                        "shimano",
                        "logo.png");

        when(brandRepository.existsBySlugAndIdNot("shimano", 1L))
                .thenReturn(false);

        when(brandRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                BrandNotFoundException.class,
                () -> brandService.update(1L, request));

        verify(brandRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete existing brand")
    void shouldDeleteBrand() {

        when(brandRepository.findById(1L))
                .thenReturn(Optional.of(brand));

        brandService.delete(1L);

        verify(brandRepository).delete(brand);
    }

    @Test
    @DisplayName("Should throw when deleting unknown brand")
    void shouldThrowWhenDeletingUnknownBrand() {

        when(brandRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                BrandNotFoundException.class,
                () -> brandService.delete(1L));

        verify(brandRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should contain correct message for BrandNotFoundException")
    void shouldContainCorrectMessageForBrandNotFoundException() {

        BrandNotFoundException exception = assertThrows(
                BrandNotFoundException.class,
                () -> {
                    throw new BrandNotFoundException(1L);
                });

        assertEquals(
                "Brand with id '1' was not found.",
                exception.getMessage());
    }

    @Test
    @DisplayName("Should contain correct message for BrandAlreadyExistsException")
    void shouldContainCorrectMessageForBrandAlreadyExistsException() {

        BrandAlreadyExistsException exception = assertThrows(
                BrandAlreadyExistsException.class,
                () -> {
                    throw new BrandAlreadyExistsException("shimano");
                });

        assertEquals(
                "Brand with slug 'shimano' already exists.",
                exception.getMessage());
    }

    @Test
    @DisplayName("Should save brand with values from request")
    void shouldSaveBrandWithValuesFromRequest() {

        BrandCreateRequest request =
                new BrandCreateRequest(
                        "Shimano",
                        "shimano",
                        "logo.png");

        when(brandRepository.existsBySlug("shimano"))
                .thenReturn(false);

        when(brandRepository.save(any(Brand.class)))
                .thenReturn(brand);

        brandService.create(request);

        ArgumentCaptor<Brand> captor =
                ArgumentCaptor.forClass(Brand.class);

        verify(brandRepository).save(captor.capture());

        Brand saved = captor.getValue();

        assertEquals("Shimano", saved.getName());
        assertEquals("shimano", saved.getSlug());
        assertEquals("logo.png", saved.getLogoUrl());
    }
}