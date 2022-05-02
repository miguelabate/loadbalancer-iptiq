package com.iptiq.exception;

public class NoRegisteredProvidersInLoadBalancer extends RuntimeException {

    private String errorMessage;
    public NoRegisteredProvidersInLoadBalancer(String message) {
        this.errorMessage=message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
