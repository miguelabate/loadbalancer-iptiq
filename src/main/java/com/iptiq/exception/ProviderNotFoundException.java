package com.iptiq.exception;

public class ProviderNotFoundException extends RuntimeException {

    private String errorMessage;
    public ProviderNotFoundException(String message) {
        this.errorMessage=message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
