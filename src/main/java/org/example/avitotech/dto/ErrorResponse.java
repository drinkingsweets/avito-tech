package org.example.avitotech.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private Error error;

    @Data
    @Builder
    public static class Error {
        private String code;
        private String message;
    }
}
