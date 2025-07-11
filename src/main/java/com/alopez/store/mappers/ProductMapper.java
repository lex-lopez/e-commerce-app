package com.alopez.store.mappers;

import com.alopez.store.dtos.ProductDto;
import com.alopez.store.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "categoryId", source = "category.id")
    ProductDto toDto(Product product);
}
