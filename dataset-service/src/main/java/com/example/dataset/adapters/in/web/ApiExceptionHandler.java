package com.example.dataset.adapters.in.web;

import com.example.dataset.core.domain.DatasetNotFoundException;
import com.example.dataset.core.domain.DatasetValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DatasetValidationException.class)
    ResponseEntity<ErrorResponse> handleValidation(DatasetValidationException exception,
                                                   HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(DatasetNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(DatasetNotFoundException exception,
                                                 HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException exception,
                                                        HttpServletRequest request) {
        String message = switch (exception.getParameterName()) {
            case "name" -> "Dataset name is required.";
            case "file" -> "CSV file is required.";
            default -> "Required request parameter '" + exception.getParameterName() + "' is missing.";
        };
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException exception,
                                                    HttpServletRequest request) {
        String message = "file".equals(exception.getRequestPartName())
                ? "CSV file is required."
                : "Required request part '" + exception.getRequestPartName() + "' is missing.";
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MultipartException.class)
    ResponseEntity<ErrorResponse> handleMultipart(MultipartException exception,
                                                  HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Invalid multipart upload request.", request);
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
