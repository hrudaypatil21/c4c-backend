package com.tisd.c4change.CustomException;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
