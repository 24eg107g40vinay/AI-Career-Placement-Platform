package com.example.career.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EmailAlreadyRegisteredException() {
        super("An account with this email already exists");
    }
}
