package com.alopez.store.services;

import com.alopez.store.dtos.OrderDto;
import com.alopez.store.exceptions.OrderNotFoundException;
import com.alopez.store.mappers.OrderMapper;
import com.alopez.store.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class OrderService {
    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public List<OrderDto> getOrders() {
        var user = authService.getCurrentUser();
        var orders = orderRepository.getOrdersByCustomer(user);

        return orders.stream().map(orderMapper::toDto).toList();
    }

    public OrderDto getOrderById(Long id) {
        var order = orderRepository
                .getOrderWithItems(id)
                .orElseThrow(OrderNotFoundException::new);

        var user = authService.getCurrentUser();
        if ( !order.isPlacedBy(user) ){
            throw new AccessDeniedException("You don't have permission to access this order.");
        }

        return orderMapper.toDto(order);
    }
}
