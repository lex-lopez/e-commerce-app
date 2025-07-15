package com.alopez.store.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartProductDto {
    private Long Id;
    private String name;
    private BigDecimal price;
}
