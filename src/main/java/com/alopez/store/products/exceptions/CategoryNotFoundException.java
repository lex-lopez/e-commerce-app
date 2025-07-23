package com.alopez.store.products.exceptions;

public class CategoryNotFoundException extends RuntimeException{
    public CategoryNotFoundException() {super("Category not found!");}
}
