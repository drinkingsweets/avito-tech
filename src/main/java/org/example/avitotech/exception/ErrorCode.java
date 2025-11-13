package org.example.avitotech.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum ErrorCode {
    TEAM_EXISTS(
            "TEAM_EXISTS",
            "team_name already exists",
            400,
            ErrorCategory.CLIENT_ERROR
    ),

    PR_EXISTS(
            "PR_EXISTS",
            "PR id already exists",
            400,
            ErrorCategory.CLIENT_ERROR
    ),

    UNAUTHORIZED(
            "UNAUTHORIZED",
            "Invalid or missing token",
            401,
            ErrorCategory.AUTHENTICATION
    ),

    NOT_FOUND(
            "NOT_FOUND",
            "resource not found",
            404,
            ErrorCategory.NOT_FOUND
    ),

    PR_MERGED(
            "PR_MERGED",
            "cannot reassign on merged PR",
            409,
            ErrorCategory.CONFLICT
    ),

    NOT_ASSIGNED(
            "NOT_ASSIGNED",
            "reviewer is not assigned to this PR",
            409,
            ErrorCategory.CONFLICT
    ),

    NO_CANDIDATE(
            "NO_CANDIDATE",
            "no active replacement candidate in team",
            409,
            ErrorCategory.CONFLICT
    );

    private final String code;
    private final String message;
    private final int httpStatus;
    private final ErrorCategory category;

    private static final Map<String, ErrorCode> CODE_CACHE = new HashMap<>();

    static {
        for (ErrorCode errorCode : ErrorCode.values()) {
            CODE_CACHE.put(errorCode.code, errorCode);
        }
    }

    ErrorCode(String code, String message, int httpStatus, ErrorCategory category) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
        this.category = category;
    }

    public boolean isClientError() {
        return httpStatus >= 400 && httpStatus < 500;
    }

    public boolean isServerError() {
        return httpStatus >= 500 && httpStatus < 600;
    }

    public String getSolutionHint() {
        switch (this) {
            case TEAM_EXISTS:
                return "Use a different team name or update the existing team";
            case PR_EXISTS:
                return "Use a different pull request ID or check if PR already exists";
            case UNAUTHORIZED:
                return "Provide a valid authentication token (Bearer token)";
            case NOT_FOUND:
                return "Verify that the resource (team, user, PR) exists";
            case PR_MERGED:
                return "Cannot modify reviewers on merged PRs. Create a new PR if needed";
            case NOT_ASSIGNED:
                return "The specified user is not assigned as a reviewer to this PR";
            case NO_CANDIDATE:
                return "No active team members available for reassignment. Activate more team members";
            default:
                return "Unknown error";
        }
    }

    @Getter
    public enum ErrorCategory {
        CLIENT_ERROR("Client Error", "Ошибка на стороне клиента"),
        AUTHENTICATION("Authentication", "Ошибка аутентификации"),
        NOT_FOUND("Not Found", "Ресурс не найден"),
        CONFLICT("Conflict", "Конфликт состояния"),
        SERVER_ERROR("Server Error", "Ошибка на стороне сервера");

        private final String name;
        private final String description;

        ErrorCategory(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}

