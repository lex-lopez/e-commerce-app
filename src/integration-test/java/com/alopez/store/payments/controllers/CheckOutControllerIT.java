
package com.alopez.store.payments.controllers;

import com.alopez.store.carts.exceptions.CartEmptyException;
import com.alopez.store.carts.exceptions.CartNotFoundException;

import java.util.HashMap;
import java.util.UUID;
import com.alopez.store.payments.dtos.CheckOutRequest;
import com.alopez.store.payments.dtos.CheckOutResponse;
import com.alopez.store.payments.dtos.WebhookRequest;
import com.alopez.store.payments.exceptions.PaymentException;
import com.alopez.store.payments.services.CheckOutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CheckOutControllerIT {

    @Mock
    private CheckOutService checkOutService;

    @InjectMocks
    private CheckOutController checkOutController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(checkOutController)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void httpCheckOut_WithValidRequest_ReturnsCheckoutResponse() throws Exception {
        // Given
        CheckOutRequest request = new CheckOutRequest();
        request.setCartId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        CheckOutResponse response = new CheckOutResponse(1L, "https://checkout.example.com/session123");

        when(checkOutService.checkOut(any(CheckOutRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.example.com/session123"));

        verify(checkOutService).checkOut(any(CheckOutRequest.class));
    }

    @Test
    void httpCheckOut_WithCartNotFound_ReturnsNotFound() throws Exception {
        // Given
        CheckOutRequest request = new CheckOutRequest();
        request.setCartId(UUID.fromString("00000000-0000-0000-0000-000000000999"));

        when(checkOutService.checkOut(any(CheckOutRequest.class))).thenThrow(new CartNotFoundException());

        // When & Then
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Cart not found!"));

        verify(checkOutService).checkOut(any(CheckOutRequest.class));
    }

    @Test
    void httpCheckOut_WithEmptyCart_ReturnsBadRequest() throws Exception {
        // Given
        CheckOutRequest request = new CheckOutRequest();
        request.setCartId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        when(checkOutService.checkOut(any(CheckOutRequest.class))).thenThrow(new CartEmptyException());

        // When & Then
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Cart is empty, please add products before checkout!"));

        verify(checkOutService).checkOut(any(CheckOutRequest.class));
    }

    @Test
    void httpCheckOut_WithPaymentException_ReturnsServerError() throws Exception {
        // Given
        CheckOutRequest request = new CheckOutRequest();
        request.setCartId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        when(checkOutService.checkOut(any(CheckOutRequest.class)))
                .thenThrow(new PaymentException("Payment processing failed"));

        // When & Then
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Error creating a checkout session, please try again later. Payment processing failed"));

        verify(checkOutService).checkOut(any(CheckOutRequest.class));
    }

    @Test
    void httpWebhook_WithValidRequest_ReturnsOk() throws Exception {
        // Given
        WebhookRequest request = new WebhookRequest(new HashMap<>(), "{\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"pi_123\",\"metadata\":{\"order_id\":\"1\"}}}}");

        doNothing().when(checkOutService).handleWebhookEvent(any(WebhookRequest.class));

        // When & Then
        mockMvc.perform(post("/api/checkout/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(checkOutService).handleWebhookEvent(any(WebhookRequest.class));
    }
}