package com.tiagofsps.hunting_fishing_store.payment.controller;

import com.tiagofsps.hunting_fishing_store.payment.dto.request.PaymentRequest;
import com.tiagofsps.hunting_fishing_store.payment.dto.response.PaymentResponse;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentStatus;
import com.tiagofsps.hunting_fishing_store.payment.gateway.PaymentGateway;
import com.tiagofsps.hunting_fishing_store.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment initiation and status")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initiate a payment (amount is read server-side from the order)")
    public PaymentResponse initiatePayment(@Valid @RequestBody PaymentRequest request) {
        return paymentService.initiatePayment(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment status")
    public PaymentResponse findById(@PathVariable Long id) {
        return paymentService.findById(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Update payment status")
    public PaymentResponse updateStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {
        return paymentService.updateStatus(id, status);
    }

    @PostMapping("/webhook")
    @Operation(summary = "Provider webhook callback (signature-verified)")
    public PaymentResponse handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = PaymentGateway.SIGNATURE_HEADER, required = false) String signature) {
        return paymentService.handleWebhook(payload, signature);
    }
}