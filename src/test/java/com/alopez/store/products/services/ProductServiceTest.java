
package com.alopez.store.products.services;

import com.alopez.store.products.dtos.Category;
import com.alopez.store.products.dtos.ProductDto;
import com.alopez.store.products.dtos.Product;
import com.alopez.store.products.exceptions.CategoryNotFoundException;
import com.alopez.store.products.exceptions.ProductNotFoundException;
import com.alopez.store.products.mappers.ProductMapper;
import com.alopez.store.products.repositories.CategoryRepository;
import com.alopez.store.products.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDto testProductDto;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId((byte) 1);
        testCategory.setName("Electronics");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setCategory(testCategory);

        testProductDto = new ProductDto();
        testProductDto.setId(1L);
        testProductDto.setName("Test Product");
        testProductDto.setDescription("Test Description");
        testProductDto.setPrice(new BigDecimal("99.99"));
        testProductDto.setCategoryId((byte) 1);
    }

    @Test
    void getAllProducts_WithCategoryId_ReturnsFilteredProducts() {
        // Given
        byte categoryId = 1;
        List<Product> products = Collections.singletonList(testProduct);
        when(productRepository.findByCategoryId(categoryId)).thenReturn(products);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When
        List<ProductDto> result = productService.getAllProducts(categoryId);

        // Then
        assertEquals(1, result.size());
        assertEquals(testProductDto, result.get(0));
        verify(productRepository).findByCategoryId(categoryId);
        verify(productRepository, never()).findAllWithCategory();
        verify(productMapper).toDto(testProduct);
    }

    @Test
    void getAllProducts_WithoutCategoryId_ReturnsAllProducts() {
        // Given
        List<Product> products = Collections.singletonList(testProduct);
        when(productRepository.findAllWithCategory()).thenReturn(products);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When
        List<ProductDto> result = productService.getAllProducts(null);

        // Then
        assertEquals(1, result.size());
        assertEquals(testProductDto, result.get(0));
        verify(productRepository).findAllWithCategory();
        verify(productRepository, never()).findByCategoryId(any());
        verify(productMapper).toDto(testProduct);
    }

    @Test
    void getAllProducts_WithEmptyResult_ReturnsEmptyList() {
        // Given
        when(productRepository.findAllWithCategory()).thenReturn(Collections.emptyList());

        // When
        List<ProductDto> result = productService.getAllProducts(null);

        // Then
        assertTrue(result.isEmpty());
        verify(productRepository).findAllWithCategory();
    }

    @Test
    void getProductById_WithExistingProduct_ReturnsProductDto() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When
        ProductDto result = productService.getProductById(1L);

        // Then
        assertEquals(testProductDto, result);
        verify(productRepository).findById(1L);
        verify(productMapper).toDto(testProduct);
    }

    @Test
    void getProductById_WithNonExistingProduct_ThrowsProductNotFoundException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(1L));
        verify(productRepository).findById(1L);
        verify(productMapper, never()).toDto(any());
    }

    @Test
    void createProduct_WithValidData_ReturnsCreatedProduct() {
        // Given
        ProductDto createRequest = new ProductDto();
        createRequest.setName("New Product");
        createRequest.setDescription("New Description");
        createRequest.setPrice(new BigDecimal("149.99"));
        createRequest.setCategoryId((byte) 1);

        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setDescription("New Description");
        newProduct.setPrice(new BigDecimal("149.99"));

        when(categoryRepository.findById((byte) 1)).thenReturn(Optional.of(testCategory));
        when(productMapper.toEntity(createRequest)).thenReturn(newProduct);
        when(productRepository.save(newProduct)).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // When
        ProductDto result = productService.createProduct(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("New Product", result.getName());
        verify(categoryRepository).findById((byte) 1);
        verify(productMapper).toEntity(createRequest);
        verify(productRepository).save(newProduct);
        assertEquals(testCategory, newProduct.getCategory());
    }

    @Test
    void createProduct_WithNonExistingCategory_ThrowsCategoryNotFoundException() {
        // Given
        ProductDto createRequest = new ProductDto();
        createRequest.setCategoryId((byte) 99);

        when(categoryRepository.findById((byte) 99)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CategoryNotFoundException.class, () -> productService.createProduct(createRequest));
        verify(categoryRepository).findById((byte) 99);
        verify(productMapper, never()).toEntity(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_WithValidData_ReturnsUpdatedProduct() {
        // Given
        ProductDto updateRequest = new ProductDto();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(new BigDecimal("199.99"));
        updateRequest.setCategoryId((byte) 1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById((byte) 1)).thenReturn(Optional.of(testCategory));

        // When
        ProductDto result = productService.updateProduct(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productRepository).findById(1L);
        verify(categoryRepository).findById((byte) 1);
        verify(productMapper).update(updateRequest, testProduct);
        verify(productRepository).save(testProduct);
        assertEquals(testCategory, testProduct.getCategory());
    }

    @Test
    void updateProduct_WithNonExistingProduct_ThrowsProductNotFoundException() {
        // Given
        ProductDto updateRequest = new ProductDto();
        updateRequest.setCategoryId((byte) 1);

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(1L, updateRequest));
        verify(productRepository).findById(1L);
        verify(categoryRepository, never()).findById(any());
        verify(productMapper, never()).update(any(), any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_WithNonExistingCategory_ThrowsCategoryNotFoundException() {
        // Given
        ProductDto updateRequest = new ProductDto();
        updateRequest.setCategoryId((byte) 99);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById((byte) 99)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CategoryNotFoundException.class, () -> productService.updateProduct(1L, updateRequest));
        verify(productRepository).findById(1L);
        verify(categoryRepository).findById((byte) 99);
        verify(productMapper, never()).update(any(), any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_VerifyCorrectFieldsUpdated() {
        // Given
        ProductDto updateRequest = new ProductDto();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(new BigDecimal("199.99"));
        updateRequest.setCategoryId((byte) 1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById((byte) 1)).thenReturn(Optional.of(testCategory));

        // Mock the update method to actually update the testProduct
        doAnswer(invocation -> {
            ProductDto dto = invocation.getArgument(0);
            Product product = invocation.getArgument(1);
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            return null;
        }).when(productMapper).update(any(), any());

        // When
        productService.updateProduct(1L, updateRequest);

        // Then
        assertEquals("Updated Product", testProduct.getName());
        assertEquals("Updated Description", testProduct.getDescription());
        assertEquals(new BigDecimal("199.99"), testProduct.getPrice());
        assertEquals(testCategory, testProduct.getCategory());
    }

    @Test
    void deleteProduct_WithExistingProduct_DeletesProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        productService.deleteProduct(1L);

        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).delete(testProduct);
    }

    @Test
    void deleteProduct_WithNonExistingProduct_ThrowsProductNotFoundException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(1L));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).delete(any());
    }
}