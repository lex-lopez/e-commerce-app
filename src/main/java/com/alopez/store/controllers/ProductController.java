package com.alopez.store.controllers;

import com.alopez.store.dtos.ErrorDto;
import com.alopez.store.dtos.ProductDto;
import com.alopez.store.exceptions.CategoryNotFoundException;
import com.alopez.store.exceptions.ProductNotFoundException;
import com.alopez.store.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Operations for products")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Gets all products")
    public List<ProductDto> getAllProducts(
            @Parameter(description = "Filter products by category id")
            @RequestParam(name = "categoryId", required = false) Byte categoryId
    ) {
        return productService.getAllProducts(categoryId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets a product by id")
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "The id of the product", required = true)
            @PathVariable Long id
    ) {
        var productDto = productService.getProductById(id);
        return ResponseEntity.ok(productDto);
    }

    @PostMapping
    @Operation(summary = "Creates a new product")
    public ResponseEntity<ProductDto> createProduct(
            @RequestBody ProductDto request,
            UriComponentsBuilder uriBuilder
    ) {
        var productDto = productService.createProduct(request);
        var uri = uriBuilder.path("/products/{id}").buildAndExpand(productDto.getId()).toUri();
        return ResponseEntity.created(uri).body(productDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates a product")
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "The id of the product", required = true)
            @PathVariable Long id,
            @RequestBody ProductDto request
    ) {
        var productDto = productService.updateProduct(id, request);
        return ResponseEntity.ok(productDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a product")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "The id of the product", required = true)
            @PathVariable Long id
    ) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorDto> handleProductNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Product not found!"));
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCategoryNotFound() {
        return ResponseEntity.badRequest().body(new ErrorDto("Category not found!"));
    }

}
