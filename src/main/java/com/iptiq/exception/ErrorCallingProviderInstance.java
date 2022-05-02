package com.iptiq.exception;

public class ErrorCallingProviderInstance extends RuntimeException {

    private String errorMessage;
    public ErrorCallingProviderInstance(String message) {
        this.errorMessage=message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
