package com.tiagofsps.hunting_fishing_store.payment.controller;

import com.tiagofsps.hunting_fishing_store.payment.dto.request.PaymentRequest;
import com.tiagofsps.hunting_fishing_store.payment.dto.response.PaymentResponse;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentMethod;
import com.tiagofsps.hunting_fishing_store.payment.entity.PaymentStatus;
import com.tiagofsps.hunting_fishing_store.payment.service.PaymentService;
import com.tiagofsps.hunting_fishing_store.common.exception.PaymentNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void shouldInitiatePayment() throws Exception {
        PaymentResponse response = new PaymentResponse(1L, 1L, BigDecimal.valueOf(100.0), PaymentMethod.MBWAY, PaymentStatus.PENDING, "tx-123");
        when(paymentService.initiatePayment(any(PaymentRequest.class))).thenReturn(response);

        String request = """
                {
                    "orderId": 1,
                    "paymentMethod": "MBWAY",
                    "amount": 100.0
                }
                """;

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("tx-123"));
    }

    @Test
    void shouldReturnNotFoundWhenPaymentDoesNotExist() throws Exception {
        when(paymentService.findById(1L)).thenThrow(new PaymentNotFoundException(1L));

        mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/payments/1/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk());
    }
}
