package com.alopez.store.mappers;

import com.alopez.store.dtos.OrderDto;
import com.alopez.store.entities.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDto toDto(Order order);
}
