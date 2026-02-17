package com.hackathon.delivery.shared.exception;

public class NoCouriersAvailableException extends RuntimeException {
    public NoCouriersAvailableException(String message) {
        super(message);
    }
}
