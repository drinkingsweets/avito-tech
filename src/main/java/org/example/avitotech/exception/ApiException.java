package org.example.avitotech.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String customMessage;


    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public ApiException(ErrorCode errorCode, String customMessage) {
        super(customMessage != null ? customMessage : errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
    public ApiException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage != null ? customMessage : errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    public ApiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public String getDebugDescription() {
        return String.format(
                "ApiException {\n" +
                        "  code: %s\n" +
                        "  message: %s\n" +
                        "  customMessage: %s\n" +
                        "  httpStatus: %d\n" +
                        "  category: %s\n" +
                        "  solution: %s\n" +
                        "}",
                errorCode.getCode(),
                errorCode.getMessage(),
                customMessage != null ? customMessage : "null",
                errorCode.getHttpStatus(),
                errorCode.getCategory().getName(),
                errorCode.getSolutionHint()
        );
    }

    public boolean hasCustomMessage() {
        return customMessage != null && !customMessage.isEmpty();
    }

    public String getErrorMessage() {
        return hasCustomMessage() ? customMessage : errorCode.getMessage();
    }

    public String getLogDescription() {
        return String.format(
                "[%s] %s (HTTP %d, Category: %s)",
                errorCode.getCode(),
                getErrorMessage(),
                errorCode.getHttpStatus(),
                errorCode.getCategory().getName()
        );
    }
}

