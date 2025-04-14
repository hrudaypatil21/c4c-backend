package com.tisd.c4change.CustomException;

// FileStorageException.java
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}