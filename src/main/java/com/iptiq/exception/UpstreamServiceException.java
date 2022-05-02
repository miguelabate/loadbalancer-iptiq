package com.iptiq.exception;

public class UpstreamServiceException extends RuntimeException {

    private String errorMessage;
    public UpstreamServiceException(String message) {
        this.errorMessage=message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
