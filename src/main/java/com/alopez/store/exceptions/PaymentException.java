package com.alopez.store.exceptions;

public class PaymentException extends RuntimeException {
    public PaymentException(String message) { super("Payment method used: " + message);}
}
