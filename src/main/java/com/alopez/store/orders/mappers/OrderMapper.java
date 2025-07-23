package com.alopez.store.orders.mappers;

import com.alopez.store.orders.dtos.OrderDto;
import com.alopez.store.orders.entities.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDto toDto(Order order);
}
