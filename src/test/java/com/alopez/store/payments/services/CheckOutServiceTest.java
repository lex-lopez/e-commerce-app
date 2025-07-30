package com.alopez.store.payments.services;

import com.alopez.store.auth.services.AuthService;
import com.alopez.store.carts.entities.Cart;
import com.alopez.store.carts.entities.CartItem;
import com.alopez.store.carts.exceptions.CartEmptyException;
import com.alopez.store.carts.exceptions.CartNotFoundException;
import com.alopez.store.carts.repositories.CartRepository;
import com.alopez.store.carts.services.CartService;
import com.alopez.store.orders.entities.Order;
import com.alopez.store.orders.entities.PaymentStatus;
import com.alopez.store.orders.repositories.OrderRepository;
import com.alopez.store.payments.dtos.CheckOutRequest;
import com.alopez.store.payments.dtos.CheckOutResponse;
import com.alopez.store.payments.dtos.CheckoutSession;
import com.alopez.store.payments.dtos.PaymentResult;
import com.alopez.store.payments.dtos.WebhookRequest;
import com.alopez.store.payments.exceptions.PaymentException;
import com.alopez.store.products.dtos.Product;
import com.alopez.store.users.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckOutServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private AuthService authService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CheckOutService checkOutService;

    private User testUser;
    private Cart testCart;
    private Order testOrder;
    private CheckOutRequest checkOutRequest;
    private CheckoutSession checkoutSession;
    private WebhookRequest webhookRequest;
    private PaymentResult paymentResult;

    private static final UUID CART_TEST_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        // Set up a test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        // Set up a test product
        Product testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("29.99"));
        
        // Set up a test cart item
        CartItem testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setQuantity(2);
        testCartItem.setProduct(testProduct);
        
        // Set up a test cart with items
        testCart = new Cart();
        testCart.setId(CART_TEST_UUID);
        testCart.setItems(Set.of(testCartItem));

        // Set up test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomer(testUser);
        testOrder.setStatus(PaymentStatus.PENDING);

        // Set up a test request
        checkOutRequest = new CheckOutRequest();
        checkOutRequest.setCartId(CART_TEST_UUID);

        // Set up a checkout session
        checkoutSession = new CheckoutSession("https://checkout.example.com/session123");

        // Set up a webhook request and payment result
        webhookRequest = new WebhookRequest(new HashMap<>(), "{\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"pi_123\",\"metadata\":{\"order_id\":\"1\"}}}}");
        paymentResult = new PaymentResult(1L, PaymentStatus.PAID);
    }

    @Test
    void checkOut_WithValidCart_ReturnsCheckoutUrl() {
        // Given
        when(cartRepository.getCartWithItems(CART_TEST_UUID)).thenReturn(Optional.of(testCart));
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(paymentGateway.createCheckoutSession(any(Order.class))).thenReturn(checkoutSession);

        doNothing().when(cartService).clearCart(CART_TEST_UUID);

        // When
        CheckOutResponse response = checkOutService.checkOut(checkOutRequest);

        // Then
        assertNotNull(response);
        assertEquals("https://checkout.example.com/session123", response.getCheckoutUrl());
        
        verify(cartRepository).getCartWithItems(CART_TEST_UUID);
        verify(authService).getCurrentUser();
        verify(orderRepository).save(any(Order.class));
        verify(paymentGateway).createCheckoutSession(any(Order.class));
        verify(cartService).clearCart(CART_TEST_UUID);
    }

    @Test
    void checkOut_WithNonExistingCart_ThrowsCartNotFoundException() {
        // Given
        when(cartRepository.getCartWithItems(CART_TEST_UUID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CartNotFoundException.class, () -> checkOutService.checkOut(checkOutRequest));
        
        verify(cartRepository).getCartWithItems(CART_TEST_UUID);
        verify(orderRepository, never()).save(any());
        verify(paymentGateway, never()).createCheckoutSession(any());
        verify(cartService, never()).clearCart(any(UUID.class));
    }

    @Test
    void checkOut_WithEmptyCart_ThrowsCartEmptyException() {
        // Given
        testCart.setItems(Collections.emptySet());
        when(cartRepository.getCartWithItems(CART_TEST_UUID)).thenReturn(Optional.of(testCart));

        // When & Then
        assertThrows(CartEmptyException.class, () -> checkOutService.checkOut(checkOutRequest));
        
        verify(cartRepository).getCartWithItems(CART_TEST_UUID);
        verify(orderRepository, never()).save(any());
        verify(paymentGateway, never()).createCheckoutSession(any());
        verify(cartService, never()).clearCart(any(UUID.class));
    }

    @Test
    void checkOut_WithPaymentException_DeletesOrderAndRethrows() {
        // Given
        when(cartRepository.getCartWithItems(CART_TEST_UUID)).thenReturn(Optional.of(testCart));
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(paymentGateway.createCheckoutSession(any(Order.class))).thenThrow(new PaymentException("Payment processing failed"));

        // When & Then
        assertThrows(PaymentException.class, () -> checkOutService.checkOut(checkOutRequest));
        
        verify(cartRepository).getCartWithItems(CART_TEST_UUID);
        verify(authService).getCurrentUser();
        verify(orderRepository).save(any(Order.class));
        verify(orderRepository).delete(any(Order.class));
        verify(paymentGateway).createCheckoutSession(any(Order.class));
        verify(cartService, never()).clearCart(any(UUID.class));
    }

    @Test
    void handleWebhookEvent_WithValidPaymentResult_UpdatesOrderStatus() {
        // Given
        when(paymentGateway.parseWebhookRequest(webhookRequest)).thenReturn(Optional.of(paymentResult));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        checkOutService.handleWebhookEvent(webhookRequest);

        // Then
        assertEquals(PaymentStatus.PAID, testOrder.getStatus());
        
        verify(paymentGateway).parseWebhookRequest(webhookRequest);
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void handleWebhookEvent_WithEmptyPaymentResult_DoesNothing() {
        // Given
        when(paymentGateway.parseWebhookRequest(webhookRequest)).thenReturn(Optional.empty());

        // When
        checkOutService.handleWebhookEvent(webhookRequest);

        // Then
        verify(paymentGateway).parseWebhookRequest(webhookRequest);
        verify(orderRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void handleWebhookEvent_VerifyOrderStatusUpdate() {
        // Given
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        
        when(paymentGateway.parseWebhookRequest(webhookRequest)).thenReturn(Optional.of(paymentResult));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        checkOutService.handleWebhookEvent(webhookRequest);

        // Then
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        
        assertEquals(PaymentStatus.PAID, savedOrder.getStatus());
        assertSame(testOrder, savedOrder);
    }
}