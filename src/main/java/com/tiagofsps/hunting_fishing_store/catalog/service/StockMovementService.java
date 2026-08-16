package com.tiagofsps.hunting_fishing_store.catalog.service;

import com.tiagofsps.hunting_fishing_store.catalog.dto.request.StockMovementRequest;
import com.tiagofsps.hunting_fishing_store.catalog.dto.response.StockMovementResponse;
import com.tiagofsps.hunting_fishing_store.catalog.entity.ProductItem;
import com.tiagofsps.hunting_fishing_store.catalog.entity.StockMovement;
import com.tiagofsps.hunting_fishing_store.catalog.repository.ProductItemRepository;
import com.tiagofsps.hunting_fishing_store.catalog.repository.StockMovementRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.InsufficientStockException;
import com.tiagofsps.hunting_fishing_store.common.exception.ProductNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.exception.UserNotFoundException;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductItemRepository productItemRepository;
    private final UserRepository userRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository, 
                                ProductItemRepository productItemRepository, 
                                UserRepository userRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.productItemRepository = productItemRepository;
        this.userRepository = userRepository;
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProductItem().getId(),
                movement.getProductItem().getSku(),
                movement.getQuantity(),
                movement.getType(),
                movement.getNotes(),
                movement.getUser() != null ? movement.getUser().getId() : null,
                movement.getCreatedAt()
        );
    }

    @Transactional
    public StockMovementResponse recordMovement(StockMovementRequest request) {
        ProductItem item = productItemRepository.findById(request.productItemId())
                .orElseThrow(() -> new ProductNotFoundException(request.productItemId()));

        User user = null;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new UserNotFoundException(request.userId()));
        }

        StockMovement movement = new StockMovement();
        movement.setProductItem(item);
        movement.setQuantity(request.quantity());
        movement.setType(request.type());
        movement.setNotes(request.notes());
        movement.setUser(user);

        // Update product item stock and prevent negative values
        int newQuantity = item.getStockQuantity() + request.quantity();
        if (newQuantity < 0) {
            throw new InsufficientStockException(item.getSku(), Math.abs(request.quantity()), item.getStockQuantity());
        }

        item.setStockQuantity(newQuantity);
        productItemRepository.save(item);

        StockMovement saved = stockMovementRepository.save(movement);
        return toResponse(saved);
    }

    public List<StockMovementResponse> getMovementsByProductItem(Long productItemId) {
        return stockMovementRepository.findByProductItemId(productItemId).stream()
                .map(this::toResponse)
                .toList();
    }
}
