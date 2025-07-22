package com.alopez.store.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() { super("Product not found!"); }
}
