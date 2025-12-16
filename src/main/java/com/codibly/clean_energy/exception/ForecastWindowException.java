package com.codibly.clean_energy.exception;

public class ForecastWindowException extends RuntimeException {
    public ForecastWindowException(int hours) {
        super(String.format("Not enough data to calculate charging window for %d hours", hours));
    }
}
