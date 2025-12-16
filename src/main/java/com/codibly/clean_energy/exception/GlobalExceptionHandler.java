package com.codibly.clean_energy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EnergyMixClientException.class)
    public ResponseEntity<String> handleEnergyMixClient(EnergyMixClientException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Failed to fetch data from external API");
    }

    @ExceptionHandler(ForecastWindowException.class)
    public ResponseEntity<String> handleForecastWindow(ForecastWindowException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Not enough data to calculate the charging window");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOtherExceptions(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error occurred");
    }
}
