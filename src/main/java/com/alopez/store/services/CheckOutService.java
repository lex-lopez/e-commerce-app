package com.alopez.store.services;

import com.alopez.store.dtos.CheckOutRequest;
import com.alopez.store.dtos.CheckOutResponse;
import com.alopez.store.entities.Order;
import com.alopez.store.exceptions.CartEmptyException;
import com.alopez.store.exceptions.CartNotFoundException;
import com.alopez.store.repositories.CartRepository;
import com.alopez.store.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CheckOutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final CartService cartService;

    public CheckOutResponse checkOut(CheckOutRequest request) {
        var cart = cartRepository.getCartWithItems(request.getCartId()).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }

        if (cart.isEmpty()) {
            throw new CartEmptyException();
        }

        var order = Order.fromCart(cart, authService.getCurrentUser());

        orderRepository.save(order);
        cartService.clearCart(cart.getId());

        return new CheckOutResponse(order.getId());
    }
}
