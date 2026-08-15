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
    @DisplayName("Should delete product")
    void shouldDeleteProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).delete(product);
    }
}
