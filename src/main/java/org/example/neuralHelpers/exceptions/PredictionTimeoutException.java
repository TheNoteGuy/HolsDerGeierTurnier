package org.example.neuralHelpers.exceptions;

public class PredictionTimeoutException extends RuntimeException {
    public PredictionTimeoutException(String message) {
        super(message);
    }
    
}
