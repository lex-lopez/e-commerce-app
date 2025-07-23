package com.alopez.store.carts.exceptions;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException() { super("Cart not found!");}
}
