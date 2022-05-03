package com.iptiq.exception;

public class ErrorCallingProviderInstanceException extends RuntimeException {

    private String errorMessage;
    public ErrorCallingProviderInstanceException(String message) {
        this.errorMessage=message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
