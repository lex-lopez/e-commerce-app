package com.alopez.store.exceptions;

public class UserNotAuthorizedException extends RuntimeException{
    public UserNotAuthorizedException() { super("User not authorized!"); }
}
