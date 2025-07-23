package com.alopez.store.payments.services;

import com.alopez.store.payments.dtos.CheckOutRequest;
import com.alopez.store.payments.dtos.CheckOutResponse;
import com.alopez.store.entities.Order;
import com.alopez.store.carts.exceptions.CartEmptyException;
import com.alopez.store.carts.exceptions.CartNotFoundException;
import com.alopez.store.payments.exceptions.PaymentException;
import com.alopez.store.carts.repositories.CartRepository;
import com.alopez.store.repositories.OrderRepository;
import com.alopez.store.auth.services.AuthService;
import com.alopez.store.carts.services.CartService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
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

    public void handleWebhookEvent(WebhookRequest request) {
        paymentGateway
                .parseWebhookRequest(request)
                .ifPresent( paymentResult -> {
                    var order = orderRepository.findById(paymentResult.getOrderId()).orElseThrow();
                    order.setStatus(paymentResult.getPaymentStatus());
                    orderRepository.save(order);
                });
    }
}
