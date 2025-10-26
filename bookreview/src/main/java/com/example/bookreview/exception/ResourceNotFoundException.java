package com.example.bookreview.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " с ID " + id + " не найден");
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(resource + " '" + identifier + "' не найден");
    }
}