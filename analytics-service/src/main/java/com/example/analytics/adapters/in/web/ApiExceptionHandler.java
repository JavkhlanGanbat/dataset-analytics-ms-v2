package com.example.analytics.adapters.in.web;

import com.example.analytics.core.domain.DatasetNotFoundException;
import com.example.analytics.core.domain.DatasetServiceBadRequestException;
import com.example.analytics.core.domain.DatasetServiceTimeoutException;
import com.example.analytics.core.domain.DatasetServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DatasetNotFoundException.class)
    ResponseEntity<ErrorResponse> handleDatasetNotFound(DatasetNotFoundException exception,
                                                        HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(DatasetServiceBadRequestException.class)
    ResponseEntity<ErrorResponse> handleDatasetServiceBadRequest(DatasetServiceBadRequestException exception,
                                                                 HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(DatasetServiceUnavailableException.class)
    ResponseEntity<ErrorResponse> handleDatasetServiceUnavailable(DatasetServiceUnavailableException exception,
                                                                  HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request);
    }

    @ExceptionHandler(DatasetServiceTimeoutException.class)
    ResponseEntity<ErrorResponse> handleDatasetServiceTimeout(DatasetServiceTimeoutException exception,
                                                              HttpServletRequest request) {
        return error(HttpStatus.GATEWAY_TIMEOUT, exception.getMessage(), request);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    ResponseEntity<ErrorResponse> handleOpenCircuit(CallNotPermittedException exception,
                                                    HttpServletRequest request) {
        return error(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Dataset Service is temporarily unavailable. Please try again shortly.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception exception,
                                                   HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error.", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                ));
    }
}
