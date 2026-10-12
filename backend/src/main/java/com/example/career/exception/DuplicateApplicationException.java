package com.example.career.exception;

public class DuplicateApplicationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DuplicateApplicationException() {
        super("You have already applied for this job");
    }
}
