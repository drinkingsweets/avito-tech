package org.example.avitotech.exception;

import org.example.avitotech.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorResponse.Error.builder()
                        .code("VALIDATION_ERROR")
                        .message(errorMessage)
                        .build())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ErrorResponse.Error.builder()
                        .code("BAD_REQUEST")
                        .message("Missing required parameter: " + name)
                        .build())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
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

