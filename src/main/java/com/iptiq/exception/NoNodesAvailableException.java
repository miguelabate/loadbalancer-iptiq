package com.iptiq.exception;

public class NoNodesAvailableException extends RuntimeException {

    private String errorMessage;
    public NoNodesAvailableException(String message) {
        this.errorMessage=message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
