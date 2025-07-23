package com.alopez.store.products.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() { super("Product not found!"); }
}
