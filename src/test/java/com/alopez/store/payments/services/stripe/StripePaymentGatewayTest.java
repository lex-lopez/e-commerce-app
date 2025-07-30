
package com.alopez.store.payments.services.stripe;

import com.alopez.store.orders.entities.Order;
import com.alopez.store.orders.entities.OrderItem;
import com.alopez.store.orders.entities.PaymentStatus;
import com.alopez.store.payments.dtos.CheckoutSession;
import com.alopez.store.payments.dtos.PaymentResult;
import com.alopez.store.payments.dtos.WebhookRequest;
import com.alopez.store.payments.exceptions.PaymentException;
import com.alopez.store.products.dtos.Product;
import com.alopez.store.users.entities.User;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripePaymentGatewayTest {

    @InjectMocks
    private StripePaymentGateway stripePaymentGateway;

    private Order testOrder;
    private Session mockSession;
    private Event mockEvent;
    private PaymentIntent mockPaymentIntent;
    private EventDataObjectDeserializer mockDeserializer;

    @BeforeEach
    void setUp() {
        // Configure test properties
        ReflectionTestUtils.setField(stripePaymentGateway, "websiteUrl", "https://example.com");
        ReflectionTestUtils.setField(stripePaymentGateway, "webhookSecret", "whsec_test_secret");

        // Set up a test user
        User testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        // Set up a test product
        Product testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("29.99"));

        // Set up order items
        List<OrderItem> orderItems = new ArrayList<>();
        OrderItem item1 = new OrderItem();
        item1.setProduct(testProduct);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("29.99"));

        OrderItem item2 = new OrderItem();
        item2.setProduct(testProduct);
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("49.99"));

        orderItems.add(item1);
        orderItems.add(item2);

        // Set up test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomer(testUser);
        testOrder.setItems(orderItems);
        testOrder.setStatus(PaymentStatus.PENDING);

        // Set up mock Stripe objects
        mockSession = mock(Session.class);
        mockEvent = mock(Event.class);
        mockPaymentIntent = mock(PaymentIntent.class);
        mockDeserializer = mock(EventDataObjectDeserializer.class);
    }

    @Test
    void createCheckoutSession_CreatesSessionSuccessfully() {
        // Given
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/session123");
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

            // When
            CheckoutSession result = stripePaymentGateway.createCheckoutSession(testOrder);

            // Then
            assertNotNull(result);
            assertEquals("https://checkout.stripe.com/session123", result.getCheckoutUrl());

            // Verify that create was called with appropriate params (we can't check the exact params)
            mockedSession.verify(() -> Session.create(any(SessionCreateParams.class)), times(1));
        }
    }

    @Test
    void createCheckoutSession_WithStripeException_ThrowsPaymentException() {
        // Given
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow( new PaymentException() );

            // When & Then
            assertThrows(PaymentException.class, () -> stripePaymentGateway.createCheckoutSession(testOrder));

            mockedSession.verify(() -> Session.create(any(SessionCreateParams.class)), times(1));
        }
    }

    @Test
    void parseWebhookRequest_WithValidPaymentSucceededEvent_ReturnsPaymentResult() {
        // Given
        WebhookRequest webhookRequest = new WebhookRequest(
                new HashMap<>(),
                "{\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"pi_123\",\"metadata\":{\"orderId\":\"1\"}}}}"
        );

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(
                    anyString(), any(), anyString()
            )).thenReturn(mockEvent);

            when(mockEvent.getType()).thenReturn("payment_intent.succeeded");
            when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);
            when(mockDeserializer.getObject()).thenReturn(Optional.of(mockPaymentIntent));
            when(mockPaymentIntent.getMetadata()).thenReturn(Map.of("order_id", "1"));
            when(mockEvent.getDataObjectDeserializer().getObject()).thenReturn(Optional.of(mockPaymentIntent));

            // When
            Optional<PaymentResult> result = stripePaymentGateway.parseWebhookRequest(webhookRequest);

            // Then
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getOrderId());
            assertEquals(PaymentStatus.PAID, result.get().getPaymentStatus());
        }
    }

    @Test
    void parseWebhookRequest_WithSignatureVerificationException_ThrowsPaymentException() {
        // Given
        Map<String, String> headers = new HashMap<>();
        headers.put("stripe-signature", "invalid_signature");
        String payload = "{\"id\":\"evt_123\"}";

        WebhookRequest webhookRequest = new WebhookRequest(headers, payload);

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(
                    anyString(), anyString(), anyString()
            )).thenThrow(new SignatureVerificationException("Invalid signature", null));

            // When & Then
            assertThrows(PaymentException.class, () -> stripePaymentGateway.parseWebhookRequest(webhookRequest));
        }
    }

    @Test
    void parseWebhookRequest_WithUnsupportedEventType_ReturnsEmptyOptional() {
        // Given
        Map<String, String> headers = new HashMap<>();
        headers.put("stripe-signature", "test_signature");
        String payload = "{\"id\":\"evt_123\",\"type\":\"customer.created\"}";

        WebhookRequest webhookRequest = new WebhookRequest(headers, payload);

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(
                    anyString(), anyString(), anyString()
            )).thenReturn(mockEvent);

            when(mockEvent.getType()).thenReturn("customer.created");

            // When
            Optional<PaymentResult> result = stripePaymentGateway.parseWebhookRequest(webhookRequest);

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void parseWebhookRequest_WithPaymentFailed_ReturnsFailedStatus() {
        // Given
        WebhookRequest webhookRequest = new WebhookRequest(
                new HashMap<>(),
                "{\"type\":\"payment_intent.payment_failed\",\"data\":{\"object\":{\"id\":\"pi_123\",\"metadata\":{\"orderId\":\"1\"}}}}"
        );

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(
                    anyString(), any(), anyString()
            )).thenReturn(mockEvent);

            when(mockEvent.getType()).thenReturn("payment_intent.payment_failed");
            when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);
            when(mockDeserializer.getObject()).thenReturn(Optional.of(mockPaymentIntent));
            when(mockPaymentIntent.getMetadata()).thenReturn(Map.of("order_id", "1"));

            // When
            Optional<PaymentResult> result = stripePaymentGateway.parseWebhookRequest(webhookRequest);

            // Then
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getOrderId());
            assertEquals(PaymentStatus.FAILED, result.get().getPaymentStatus());
        }
    }


    @Test
    void parseWebhookRequest_WithDeserializationError_ThrowsPaymentException() {
        // Given
        Map<String, String> headers = new HashMap<>();
        headers.put("stripe-signature", "test_signature");
        String payload = "{\"id\":\"evt_123\",\"data\":{\"object\":{\"id\":\"pi_123\"}}}";

        WebhookRequest webhookRequest = new WebhookRequest(headers, payload);

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(
                    anyString(), anyString(), anyString()
            )).thenReturn(mockEvent);

            when(mockEvent.getType()).thenReturn("payment_intent.succeeded");
            when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);
            when(mockDeserializer.getObject()).thenReturn(Optional.empty());

            // When & Then
            assertThrows(PaymentException.class, () -> stripePaymentGateway.parseWebhookRequest(webhookRequest));
        }
    }
}