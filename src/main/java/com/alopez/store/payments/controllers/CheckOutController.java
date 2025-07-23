package com.alopez.store.payments.controllers;

import com.alopez.store.dtos.ErrorDto;
import com.alopez.store.carts.exceptions.CartEmptyException;
import com.alopez.store.carts.exceptions.CartNotFoundException;
import com.alopez.store.payments.dtos.CheckOutRequest;
import com.alopez.store.payments.dtos.CheckOutResponse;
import com.alopez.store.payments.exceptions.PaymentException;
import com.alopez.store.payments.services.CheckOutService;
import com.alopez.store.payments.services.WebhookRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/checkout")
public class CheckOutController {
    private final CheckOutService checkOutService;

    @PostMapping
    public ResponseEntity<CheckOutResponse> checkout(
            @Valid @RequestBody CheckOutRequest request
    ) {
        var checkOutResponse = checkOutService.checkOut(request);
        return ResponseEntity.ok(checkOutResponse);
    }

    @PostMapping("/webhook")
    public void handleWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody String payload
    ){
        checkOutService.handleWebhookEvent(new WebhookRequest(headers, payload));
    }

    @ExceptionHandler({ CartNotFoundException.class, CartEmptyException.class })
    public ResponseEntity<ErrorDto> handleCartNotFound(Exception ex) {
        return ResponseEntity.badRequest().body(new ErrorDto(ex.getMessage()));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorDto> handlePaymentException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("Error creating a checkout session, please try again later. " + ex.getMessage()));
    }

}
