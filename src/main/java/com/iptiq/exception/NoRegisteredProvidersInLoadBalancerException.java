package com.iptiq.exception;

public class NoRegisteredProvidersInLoadBalancerException extends RuntimeException {

    private String errorMessage;
    public NoRegisteredProvidersInLoadBalancerException(String message) {
        this.errorMessage=message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
