package org.example.neuralHelpers.exceptions;

public class NetworkExecutionException extends RuntimeException {
    public NetworkExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
