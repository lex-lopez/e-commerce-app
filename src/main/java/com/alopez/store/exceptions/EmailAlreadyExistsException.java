package com.alopez.store.exceptions;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException(){ super("Email is already registered!"); }
}
