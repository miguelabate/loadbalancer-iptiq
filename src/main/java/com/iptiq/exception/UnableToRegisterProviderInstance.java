package com.iptiq.exception;

public class UnableToRegisterProviderInstance extends RuntimeException {

    private String errorMessage;
    public UnableToRegisterProviderInstance(String message) {
        this.errorMessage=message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
