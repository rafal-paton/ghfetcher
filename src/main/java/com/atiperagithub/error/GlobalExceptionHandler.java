package com.atiperagithub.error;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException() {
        log.info("User not found exception occurred.");
        return ResponseEntity.status(404)
                .body(new ErrorResponseDto(404, "User not found."));
    }

    @ExceptionHandler(WrongAcceptHeaderException.class)
    public ResponseEntity<WrongHeaderResponseDto> handleWrongHeaderException() {
        return ResponseEntity.status(406)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new WrongHeaderResponseDto(406, "Wrong header 'accept'. Only JSON is acceptable."));
    }
}