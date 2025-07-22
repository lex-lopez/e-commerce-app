package com.alopez.store.exceptions;

public class CartEmptyException extends RuntimeException {
    public CartEmptyException() { super("Cart is empty, please add products before checkout!");}
}
