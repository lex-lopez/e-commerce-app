package com.alopez.store.carts.controllers;

import com.alopez.store.carts.dtos.AddItemToCartRequest;
import com.alopez.store.carts.dtos.CartDto;
import com.alopez.store.carts.dtos.CartItemDto;
import com.alopez.store.carts.dtos.UpdateCartItemRequest;
import com.alopez.store.common.dtos.ErrorDto;
import com.alopez.store.carts.exceptions.CartNotFoundException;
import com.alopez.store.products.exceptions.ProductNotFoundException;
import com.alopez.store.carts.services.CartService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    @GetMapping("/{cartId}")
    public CartDto getCart(@PathVariable("cartId") UUID cartId) {
        return cartService.getCart(cartId);
    }

    @PostMapping
    public ResponseEntity<CartDto> createCart(UriComponentsBuilder uriBuilder) {
        var cartDto = cartService.createCart();
        var uri = uriBuilder.path("/api/cart/{id}").buildAndExpand(cartDto.getId()).toUri();
        return ResponseEntity.created(uri).body(cartDto);
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemDto> addProductToCart(
            @PathVariable("cartId") UUID cartId,
            @RequestBody AddItemToCartRequest request
    ) {
        var cartDto = cartService.addProductToCart(cartId, request.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(cartDto);
    }

    @PutMapping("/{cartId}/items/{productId}")
    public CartItemDto updateItemInCart(
            @PathVariable("cartId") UUID cartId,
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateItemInCart(cartId, productId, request.getQuantity());
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable("cartId") UUID cartId,
            @PathVariable("productId") Long productId
    ) {
        cartService.removeItemFromCart(cartId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<Void> clearCart(@PathVariable("cartId") UUID cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCartNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorDto> handleProductNotFound(Exception e) {
        return ResponseEntity.badRequest().body(new ErrorDto(e.getMessage()));
    }
}
