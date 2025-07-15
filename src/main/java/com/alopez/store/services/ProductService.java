package com.alopez.store.services;

import com.alopez.store.dtos.ProductDto;
import com.alopez.store.entities.Product;
import com.alopez.store.exceptions.CategoryNotFoundException;
import com.alopez.store.exceptions.ProductNotFoundException;
import com.alopez.store.mappers.ProductMapper;
import com.alopez.store.repositories.CategoryRepository;
import com.alopez.store.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public List<ProductDto> getAllProducts(Byte categoryId) {
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else {
            products = productRepository.findAllWithCategory();
        }

        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }

    public ProductDto getProductById(Long id) {
        var product = productRepository.findById(id).orElse(null);
        if ( product == null ) {
            throw new ProductNotFoundException();
        }

        return productMapper.toDto(product);
    }

    public ProductDto createProduct(ProductDto productDto) {
        var category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);
        if (category == null) {
            throw new CategoryNotFoundException();
        }

        var product = productMapper.toEntity(productDto);
        product.setCategory(category);
        productRepository.save(product);

        productDto.setId(product.getId());
        return productDto;
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        var product = productRepository.findById(id).orElse(null);
        if (product == null) {
            throw new ProductNotFoundException();
        }

        var category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);
        if (category == null) {
            throw new CategoryNotFoundException();
        }

        productMapper.update(productDto, product);
        product.setCategory(category);
        productRepository.save(product);

        productDto.setId(product.getId());
        return productDto;
    }

    public void deleteProduct(Long id) {
        var product = productRepository.findById(id).orElse(null);
        if (product == null) {
            throw new ProductNotFoundException();
        }
        productRepository.delete(product);
    }
}
