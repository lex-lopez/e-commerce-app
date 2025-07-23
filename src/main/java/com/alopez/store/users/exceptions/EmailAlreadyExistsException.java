package com.alopez.store.users.exceptions;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException(){ super("Email is already registered!"); }
}
