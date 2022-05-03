package com.iptiq.exception;

public class UnableToRegisterProviderInstanceException extends RuntimeException {

    private String errorMessage;
    public UnableToRegisterProviderInstanceException(String message) {
        this.errorMessage=message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
