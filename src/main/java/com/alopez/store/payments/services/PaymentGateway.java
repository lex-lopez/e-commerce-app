package com.alopez.store.payments.services;

import com.alopez.store.orders.entities.Order;
import com.alopez.store.payments.dtos.CheckoutSession;
import com.alopez.store.payments.dtos.PaymentResult;
import com.alopez.store.payments.dtos.WebhookRequest;

import java.util.Optional;

public interface PaymentGateway {
    CheckoutSession createCheckoutSession(Order order);
    Optional<PaymentResult> parseWebhookRequest(WebhookRequest request);
}
