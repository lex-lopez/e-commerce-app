package com.alopez.store.controllers;

import com.alopez.store.dtos.CheckOutRequest;
import com.alopez.store.dtos.CheckOutResponse;
import com.alopez.store.dtos.ErrorDto;
import com.alopez.store.exceptions.CartIsEmptyException;
import com.alopez.store.exceptions.CartNotFoundException;
import com.alopez.store.services.CheckOutService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCartNotFound() {
        return ResponseEntity.badRequest().body(new ErrorDto("Cart not found!"));
    }

    @ExceptionHandler(CartIsEmptyException.class)
    public ResponseEntity<ErrorDto> handleCartIsEmpty() {
        return ResponseEntity.badRequest().body(new ErrorDto("Cart is empty! Please add products to check out your cart!"));
    }
}
