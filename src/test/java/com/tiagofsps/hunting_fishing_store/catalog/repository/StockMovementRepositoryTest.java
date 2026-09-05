package com.tiagofsps.hunting_fishing_store.catalog.repository;

import com.tiagofsps.hunting_fishing_store.BaseRepositoryTest;
import com.tiagofsps.hunting_fishing_store.catalog.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StockMovementRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSaveMovement() {
        Brand brand = new Brand();
        brand.setName("B");
        brand.setSlug("b");
        brandRepository.save(brand);

        Category category = new Category();
        category.setName("C");
        category.setSlug("c");
        categoryRepository.save(category);

        Product product = new Product();
        product.setName("P");
        product.setSlug("p");
        product.setBrand(brand);
        product.setCategory(category);
        productRepository.save(product);

        ProductItem item = new ProductItem();
        item.setSku("S");
        item.setPrice(BigDecimal.TEN);
        item.setStockQuantity(0);
        item.setProduct(product);
        productItemRepository.save(item);

        StockMovement movement = new StockMovement();
        movement.setProductItem(item);
        movement.setQuantity(10);
        movement.setType(MovementType.PURCHASE);

        StockMovement saved = stockMovementRepository.save(movement);

        assertNotNull(saved.getId());
    }
}
