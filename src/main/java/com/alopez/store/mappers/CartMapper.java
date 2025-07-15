package com.alopez.store.mappers;

import com.alopez.store.dtos.CartDto;
import com.alopez.store.dtos.CartItemDto;
import com.alopez.store.entities.Cart;
import com.alopez.store.entities.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "totalPrice", expression = "java(cart.getTotalPrice())")
    CartDto toDto(Cart cart);

    @Mapping(target = "totalPrice", expression = "java(cartItem.getTotalPrice())")
    CartItemDto toDto(CartItem cartItem);
}
