package com.alopez.store.users.exceptions;

public class UserNotAuthorizedException extends RuntimeException{
    public UserNotAuthorizedException() { super("User not authorized!"); }
}
