package com.ecommerce.exception;

/**
 * Custom exception for user-related errors.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("User with email " + email + " already exists");
    }
}
