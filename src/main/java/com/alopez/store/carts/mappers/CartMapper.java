package com.alopez.store.carts.mappers;

import com.alopez.store.carts.dtos.CartDto;
import com.alopez.store.carts.dtos.CartItemDto;
import com.alopez.store.carts.entities.Cart;
import com.alopez.store.carts.entities.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "totalPrice", expression = "java(cart.getTotalPrice())")
    CartDto toDto(Cart cart);

    @Mapping(target = "totalPrice", expression = "java(cartItem.getTotalPrice())")
    CartItemDto toDto(CartItem cartItem);
}
