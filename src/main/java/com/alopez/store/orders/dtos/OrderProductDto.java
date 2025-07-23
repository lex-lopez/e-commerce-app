package com.alopez.store.orders.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderProductDto {
    private Long id;
    private String name;
    private BigDecimal price;
}
