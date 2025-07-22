package com.alopez.store.services;

import com.alopez.store.dtos.CheckOutRequest;
import com.alopez.store.dtos.CheckOutResponse;
import com.alopez.store.entities.Order;
import com.alopez.store.exceptions.CartEmptyException;
import com.alopez.store.exceptions.CartNotFoundException;
import com.alopez.store.exceptions.PaymentException;
import com.alopez.store.repositories.CartRepository;
import com.alopez.store.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CheckOutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final AuthService authService;
    private final CartService cartService;

    @Transactional
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

        try {
            var checkoutSession = paymentGateway.createCheckoutSession(order);

            cartService.clearCart(cart.getId());

            return new CheckOutResponse(order.getId(), checkoutSession.getCheckoutUrl());
        } catch (PaymentException ex) {
            orderRepository.delete(order);
            throw ex;
        }
    }
}
