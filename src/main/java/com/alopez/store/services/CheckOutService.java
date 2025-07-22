package com.alopez.store.services;

import com.alopez.store.dtos.CheckOutRequest;
import com.alopez.store.dtos.CheckOutResponse;
import com.alopez.store.entities.Order;
import com.alopez.store.entities.OrderItem;
import com.alopez.store.entities.OrderStatus;
import com.alopez.store.exceptions.CartIsEmptyException;
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

        if (cart.getItems().isEmpty()) {
            throw new CartIsEmptyException();
        }

        var order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setCustomer(authService.getCurrentUser());
        order.setTotalPrice(cart.getTotalPrice());

        cart.getItems().forEach(item -> {
           var orderItem = new OrderItem();
           orderItem.setOrder(order);
           orderItem.setProduct(item.getProduct());
           orderItem.setUnitPrice(item.getProduct().getPrice());
           orderItem.setQuantity(item.getQuantity());
           orderItem.setTotalPrice(item.getTotalPrice());

           order.getItems().add(orderItem);
        });

        orderRepository.save(order);
        cartService.clearCart(cart.getId());
        return new CheckOutResponse(order.getId());
    }
}
