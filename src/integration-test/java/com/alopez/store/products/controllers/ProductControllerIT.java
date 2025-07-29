
package com.alopez.store.products.controllers;

import com.alopez.store.products.dtos.ProductDto;
import com.alopez.store.products.exceptions.CategoryNotFoundException;
import com.alopez.store.products.exceptions.ProductNotFoundException;
import com.alopez.store.products.services.ProductService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerIT {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ProductDto testProductDto;

    @BeforeEach
    void setUp() {
        // Create standalone MockMvc - no Spring context needed
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();

        testProductDto = new ProductDto();
        testProductDto.setId(1L);
        testProductDto.setName("Test Product");
        testProductDto.setDescription("Test Description");
        testProductDto.setPrice(new BigDecimal("99.99"));
        testProductDto.setCategoryId((byte) 1);
    }

    @Test
    void httpGetProducts_WithoutCategoryFilter_ReturnsAllProducts() throws Exception {
        // Given
        when(productService.getAllProducts(null)).thenReturn(List.of(testProductDto));

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].price").value(99.99));

        verify(productService).getAllProducts(null);
    }

    @Test
    void httpGetProducts_WithCategoryFilter_ReturnsFilteredProducts() throws Exception {
        // Given
        byte categoryId = 1;
        when(productService.getAllProducts(categoryId)).thenReturn(List.of(testProductDto));

        // When & Then
        mockMvc.perform(get("/api/products").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoryId").value(1));

        verify(productService).getAllProducts(categoryId);
    }

    @Test
    void httpGetProduct_MapsPathVariableCorrectly() throws Exception {
        // Given
        when(productService.getProductById(123L)).thenReturn(testProductDto);

        // When & Then
        mockMvc.perform(get("/api/products/123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(productService).getProductById(123L);
    }

    @Test
    void httpGetProduct_WithNonExistingProduct_ReturnsNotFound() throws Exception {
        // Given
        when(productService.getProductById(999L)).thenThrow(new ProductNotFoundException());

        // When & Then
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Product not found!"));
    }

    @Test
    void httpCreateProduct_ReturnsLocationHeader() throws Exception {
        // Given
        ProductDto createRequest = new ProductDto();
        createRequest.setName("New Product");
        createRequest.setDescription("New Description");
        createRequest.setPrice(new BigDecimal("149.99"));
        createRequest.setCategoryId((byte) 1);

        ProductDto createdProduct = new ProductDto();
        createdProduct.setId(42L);
        createdProduct.setName("New Product");
        createdProduct.setDescription("New Description");
        createdProduct.setPrice(new BigDecimal("149.99"));
        createdProduct.setCategoryId((byte) 1);

        when(productService.createProduct(any(ProductDto.class))).thenReturn(createdProduct);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/products/42")));
    }

    @Test
    void httpCreateProduct_WithInvalidCategory_ReturnsBadRequest() throws Exception {
        // Given
        ProductDto createRequest = new ProductDto();
        createRequest.setName("New Product");
        createRequest.setCategoryId((byte) 99);

        when(productService.createProduct(any(ProductDto.class))).thenThrow(new CategoryNotFoundException());

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Category not found!"));
    }


    @Test
    void httpUpdateProduct_ReturnsUpdatedProduct() throws Exception {
        // Given
        ProductDto updateRequest = new ProductDto();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(new BigDecimal("199.99"));
        updateRequest.setCategoryId((byte) 2);

        // Create the expected response that reflects the updated values
        ProductDto updatedProduct = new ProductDto();
        updatedProduct.setId(1L); // The ID should remain the same
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(new BigDecimal("199.99"));
        updatedProduct.setCategoryId((byte) 2);

        when(productService.updateProduct(eq(1L), any(ProductDto.class))).thenReturn(updatedProduct);

        // When & Then
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.categoryId").value(2));

        verify(productService).updateProduct(eq(1L), any(ProductDto.class));
    }

    @Test
    void httpUpdateProduct_WithNonExistingProduct_ReturnsNotFound() throws Exception {
        // Given
        ProductDto updateRequest = new ProductDto();
        updateRequest.setName("Updated Product");
        updateRequest.setCategoryId((byte) 1);

        when(productService.updateProduct(eq(999L), any(ProductDto.class))).thenThrow(new ProductNotFoundException());

        // When & Then
        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Product not found!"));
    }

    @Test
    void httpDeleteProduct_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(productService).deleteProduct(1L);

        // When & Then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void httpDeleteProduct_WithNonExistingProduct_ReturnsNotFound() throws Exception {
        // Given
        doThrow(new ProductNotFoundException()).when(productService).deleteProduct(999L);

        // When & Then
        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Product not found!"));
    }
}