package com.alopez.store.controllers;

import com.alopez.store.dtos.CheckOutRequest;
import com.alopez.store.dtos.CheckOutResponse;
import com.alopez.store.dtos.ErrorDto;
import com.alopez.store.exceptions.CartEmptyException;
import com.alopez.store.exceptions.CartNotFoundException;
import com.alopez.store.exceptions.PaymentException;
import com.alopez.store.services.CheckOutService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
