package com.alopez.store.controllers;

import com.alopez.store.dtos.AddItemToCartRequest;
import com.alopez.store.dtos.CartDto;
import com.alopez.store.dtos.CartItemDto;
import com.alopez.store.dtos.UpdateCartItemRequest;
import com.alopez.store.exceptions.CartNotFoundException;
import com.alopez.store.exceptions.ProductNotFoundException;
import com.alopez.store.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/carts")
@Tag(name = "Carts", description = "Operations for shop carts")
public class CartController {
    private final CartService cartService;

    @GetMapping("/{cartId}")
    @Operation(summary = "Gets a shop cart by id")
    public CartDto getCart(
            @Parameter(description = "The id of the shop cart", required = true)
            @PathVariable("cartId") UUID cartId
    ) {
        return cartService.getCart(cartId);
    }

    @PostMapping
    @Operation(summary = "Creates a new shop cart")
    public ResponseEntity<CartDto> createCart(
            UriComponentsBuilder uriBuilder
    ) {
        var cartDto = cartService.createCart();
        var uri = uriBuilder.path("/api/cart/{id}").buildAndExpand(cartDto.getId()).toUri();
        return ResponseEntity.created(uri).body(cartDto);
    }

    @PostMapping("/{cartId}/items")
    @Operation(summary = "Adds a product to the shop cart")
    public ResponseEntity<CartItemDto> addProductToCart(
            @Parameter(description = "The id of the shop cart", required = true)
            @PathVariable("cartId") UUID cartId,
            @RequestBody AddItemToCartRequest request
    ) {
        var cartDto = cartService.addProductToCart(cartId, request.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(cartDto);
    }

    @PutMapping("/{cartId}/items/{productId}")
    @Operation(summary = "Updates the quantity of a product in the shop cart")
    public CartItemDto updateItemInCart(
            @Parameter(description = "The id of the shop cart", required = true)
            @PathVariable("cartId") UUID cartId,
            @Parameter(description = "The id of the product", required = true)
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateItemInCart(cartId, productId, request.getQuantity());
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    @Operation(summary = "Removes a product from the shop cart")
    public ResponseEntity<Void> removeItemFromCart(
            @Parameter(description = "The id of the shop cart", required = true)
            @PathVariable("cartId") UUID cartId,
            @Parameter(description = "The id of the product", required = true)
            @PathVariable("productId") Long productId
    ) {
        cartService.removeItemFromCart(cartId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}/items")
    @Operation(summary = "Clears the shop cart")
    public ResponseEntity<Void> clearCart(
            @Parameter(description = "The id of the shop cart", required = true)
            @PathVariable("cartId") UUID cartId
    ) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Cart not found!"));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFound() {
        return ResponseEntity.badRequest().body(Map.of("error", "Product not found in the cart!"));
    }
}
