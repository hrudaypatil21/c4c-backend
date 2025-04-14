package com.tisd.c4change.CustomException;

// InvalidCredentialsException.java
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
