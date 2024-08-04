package com.ghfetcher.error;

public class WrongAcceptHeaderException extends RuntimeException {
    public WrongAcceptHeaderException(String message) {
        super(message);
    }
}