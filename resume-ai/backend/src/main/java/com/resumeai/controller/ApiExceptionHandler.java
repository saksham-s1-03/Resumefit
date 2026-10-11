package com.resumeai.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private ResponseEntity<Map<String, String>> body(HttpStatus s, String msg) {
        return ResponseEntity.status(s).body(Map.of("error", msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> badInput(IllegalArgumentException e) { return body(HttpStatus.BAD_REQUEST, e.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> invalid(MethodArgumentNotValidException e) {
        return body(HttpStatus.BAD_REQUEST, "Enter a valid email and a password of 8 or more characters.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> status(ResponseStatusException e) {
        return body(HttpStatus.valueOf(e.getStatusCode().value()),
                e.getReason() == null ? "Request failed." : e.getReason());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<?> tooBig(MaxUploadSizeExceededException e) {
        return body(HttpStatus.BAD_REQUEST, "The file is larger than 5 MB.");
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<?> state(IllegalStateException e) { return body(HttpStatus.BAD_GATEWAY, e.getMessage()); }

    @ExceptionHandler(RestClientException.class)
    ResponseEntity<?> upstream(RestClientException e) {
        return body(HttpStatus.BAD_GATEWAY, "The AI service could not be reached. Check the API key and try again.");
    }
}
