package com.alopez.store.users.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException() {super("User not found!");}
}
