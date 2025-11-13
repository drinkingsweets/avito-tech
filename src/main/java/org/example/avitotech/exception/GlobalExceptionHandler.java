package org.example.avitotech.exception;

import org.example.avitotech.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorCode code = ex.getErrorCode();

        if (code.isClientError()) {
            log.warn("Client error: {}", ex.getLogDescription());
        } else if (code.isServerError()) {
            log.error("Server error: {}", ex.getDebugDescription(), ex);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorResponse.Error.builder()
                        .code(code.getCode())
                        .message(ex.getErrorMessage())
                        .build())
                .build();

        return new ResponseEntity<>(
                errorResponse,
                HttpStatus.valueOf(code.getHttpStatus())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorResponse.Error.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .message("An unexpected error occurred")
                        .build())
                .build();

        return new ResponseEntity<>(
                errorResponse,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}

