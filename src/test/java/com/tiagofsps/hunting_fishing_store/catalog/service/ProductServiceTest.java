package com.tiagofsps.hunting_fishing_store.catalog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.ProductCreateRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.request.ProductItemRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.ProductResponse;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Product;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Brand;
import com.tiagofsps.hunting_fishing_store.catalog.entity.Category;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.BrandRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.CategoryRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.BrandNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.CategoryNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.ProductAlreadyExistsException;
import com.tiagofsps.hunting_fishing_store.common.exception.ProductNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Brand brand;
    private Category category;

    @BeforeEach
    void setup() {
        brand = new Brand();
        brand.setId(1L);
        brand.setName("Shimano");

        category = new Category();
        category.setId(1L);
        category.setName("Fishing");

        product = new Product();
        product.setId(1L);
        product.setName("Fishing Rod");
        product.setSlug("fishing-rod");
        product.setBrand(brand);
        product.setCategory(category);
        product.setItems(new ArrayList<>());
        product.setImages(new ArrayList<>());
    }

    @Test
    @DisplayName("Should return product when id exists")
    void shouldFindProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("Fishing Rod", response.name());
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw when product not found")
    void shouldThrowWhenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById(1L));
    }

    @Test
    @DisplayName("Should return all products")
    void shouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> result = productService.findAll();

        assertEquals(1, result.size());
        assertEquals("Fishing Rod", result.getFirst().name());
    }

    @Test
    @DisplayName("Should create product with items")
    void shouldCreateProductWithItems() {
        ProductItemRequest itemReq = new ProductItemRequest("SKU001", new BigDecimal("100.00"), 10, 0.5, true, null);
        ProductCreateRequest request = new ProductCreateRequest("Fishing Rod", "fishing-rod", "Desc", false, 1L, 1L, List.of(itemReq), null);

        when(productRepository.existsBySlug("fishing-rod")).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductResponse response = productService.create(request);

        assertEquals("Fishing Rod", response.name());
        assertEquals(1, response.items().size());
        assertEquals("SKU001", response.items().getFirst().sku());
    }

    @Test
    @DisplayName("Should throw when creating duplicated slug")
    void shouldThrowWhenCreatingDuplicatedSlug() {
        ProductCreateRequest request = new ProductCreateRequest("Fishing Rod", "fishing-rod", "Desc", false, 1L, 1L, List.of(), null);
        when(productRepository.existsBySlug("fishing-rod")).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productService.create(request));
    }

    @Test
    @DisplayName("Should throw when brand not found during create")
    void shouldThrowWhenBrandNotFound() {
        ProductCreateRequest request = new ProductCreateRequest("Fishing Rod", "fishing-rod", "Desc", false, 1L, 1L, List.of(), null);
        when(productRepository.existsBySlug("fishing-rod")).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BrandNotFoundException.class, () -> productService.create(request));
    }

    @Test
    @DisplayName("Should update existing product")
    void shouldUpdateProduct() {
        ProductCreateRequest request = new ProductCreateRequest("Updated", "updated", "Desc", false, 1L, 1L, List.of(), null);
        
        when(productRepository.existsBySlugAndIdNot("updated", 1L)).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProductResponse response = productService.update(1L, request);

        assertEquals("Updated", response.name());
        assertEquals("updated", response.slug());
    }

    @Test
    @DisplayName("Should delete product")
    void shouldDeleteProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("Should throw when deleting non-existent product")
    void shouldThrowWhenDeletingNonExistent() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.delete(1L));
    }
}
