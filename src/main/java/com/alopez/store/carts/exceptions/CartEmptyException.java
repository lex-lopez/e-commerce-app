package com.alopez.store.carts.exceptions;

public class CartEmptyException extends RuntimeException {
    public CartEmptyException() { super("Cart is empty, please add products before checkout!");}
}
