package com.tiagofsps.hunting_fishing_store.catalog.service;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.StockMovementRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.StockMovementResponse;
import com.tiagofsps.hunting_fishing_store.catalog.entity.MovementType;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductItem;
import com.tiagofsps.hunting_fishing_store.catalog.entity.StockMovement;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductItemRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.StockMovementRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.InsufficientStockException;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductItemRepository productItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StockMovementService stockMovementService;

    private ProductItem productItem;

    @BeforeEach
    void setup() {
        productItem = new ProductItem();
        productItem.setId(1L);
        productItem.setSku("SKU-1");
        productItem.setStockQuantity(10);
    }

    @Test
    @DisplayName("Should record purchase movement and increase stock")
    void shouldRecordPurchase() {
        StockMovementRequest request = new StockMovementRequest(1L, 5, MovementType.PURCHASE, "New batch", null);

        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItem));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> {
            StockMovement m = i.getArgument(0);
            m.setId(1L);
            return m;
        });

        StockMovementResponse response = stockMovementService.recordMovement(request);

        assertEquals(15, productItem.getStockQuantity());
        assertEquals(MovementType.PURCHASE, response.type());
        verify(productItemRepository).save(productItem);
    }

    @Test
    @DisplayName("Should record sale movement and decrease stock")
    void shouldRecordSale() {
        StockMovementRequest request = new StockMovementRequest(1L, -3, MovementType.SALE, "Order #123", null);

        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItem));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        stockMovementService.recordMovement(request);

        assertEquals(7, productItem.getStockQuantity());
        verify(productItemRepository).save(productItem);
    }

    @Test
    @DisplayName("Should throw exception when movement results in negative stock")
    void shouldThrowWhenNegativeStock() {
        StockMovementRequest request = new StockMovementRequest(1L, -15, MovementType.SALE, "Order #123", null);

        when(productItemRepository.findById(1L)).thenReturn(Optional.of(productItem));

        assertThrows(InsufficientStockException.class, () -> stockMovementService.recordMovement(request));

        verify(productItemRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }
}
