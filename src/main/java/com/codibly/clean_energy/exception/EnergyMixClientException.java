package com.codibly.clean_energy.exception;

public class EnergyMixClientException extends RuntimeException {
    public EnergyMixClientException(String message) {
        super(message);
    }
    public EnergyMixClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
