package com.atiperagithub.error;

public class WrongAcceptHeaderException extends RuntimeException {
    public WrongAcceptHeaderException(String message) {
        super(message);
    }
}