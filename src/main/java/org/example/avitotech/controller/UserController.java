package org.example.avitotech.controller;

import org.example.avitotech.dto.PullRequestResponse;
import org.example.avitotech.dto.UserSetActiveRequest;
import org.example.avitotech.dto.PullRequestResponse;
import org.example.avitotech.exception.ApiException;
import org.example.avitotech.exception.ErrorCode;
import org.example.avitotech.model.PullRequest;
import org.example.avitotech.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/setIsActive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setUserActive(@Valid @RequestBody UserSetActiveRequest request) {
        log.info("Received request to set user active status: userId={}, isActive={}",
                request.getUserId(), request.getIsActive());

        try {
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                log.warn("User ID is empty or null");
                throw new ApiException(ErrorCode.NOT_FOUND, "User ID cannot be empty");
            }

            userService.setUserActive(request.getUserId(), request.getIsActive());

            log.info("User active status updated successfully: userId={}", request.getUserId());

            return ResponseEntity.ok(
                    Map.of("message", "User " + request.getUserId() + " status updated to " + request.getIsActive())
            );
        } catch (ApiException ex) {
            log.warn("Error setting user active status: {}", ex.getLogDescription());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while setting user active status", ex);
            throw new ApiException(
                    ErrorCode.NOT_FOUND,
                    "Unexpected error during user status update: " + ex.getMessage(),
                    ex
            );
        }
    }

    @GetMapping("/getReview")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getUserReview(@RequestParam(name = "user_id") String userId) {

        log.debug("Received request to get review PRs for user: {}", userId);

        try {
            if (userId == null || userId.trim().isEmpty()) {
                log.warn("User ID parameter is empty");
                throw new ApiException(
                        ErrorCode.NOT_FOUND,
                        "User ID cannot be empty"
                );
            }

            if (!userService.userExists(userId)) {
                log.warn("User not found: {}", userId);
                throw new ApiException(
                        ErrorCode.NOT_FOUND,
                        "User not found"
                );
            }

            List<PullRequest> pullRequests = userService.getUserPullRequests(userId);

            List<PullRequestResponse> responses = pullRequests.stream()
                    .map(PullRequestResponse::from)
                    .collect(Collectors.toList());

            log.debug("Retrieved {} pull requests for user: {}", responses.size(), userId);

            return ResponseEntity.ok(
                    Map.of("pull_requests", responses)
            );
        } catch (ApiException ex) {
            log.warn("Error retrieving user review PRs '{}': {}", userId, ex.getLogDescription());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while retrieving user review PRs '{}'", userId, ex);
            throw new ApiException(
                    ErrorCode.NOT_FOUND,
                    "Unexpected error during review PRs retrieval: " + ex.getMessage(),
                    ex
            );
        }
    }
}
