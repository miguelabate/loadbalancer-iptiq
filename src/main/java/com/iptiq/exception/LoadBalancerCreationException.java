package com.iptiq.exception;

public class LoadBalancerCreationException extends RuntimeException {

    private String errorMessage;
    public LoadBalancerCreationException(String message) {
        this.errorMessage=message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
