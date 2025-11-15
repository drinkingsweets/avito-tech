package org.example.avitotech.controller;

import org.example.avitotech.dto.PullRequestCreateRequest;
import org.example.avitotech.dto.PullRequestMergeRequest;
import org.example.avitotech.dto.PullRequestReassignRequest;
import org.example.avitotech.dto.PullRequestResponse;
import org.example.avitotech.exception.ApiException;
import org.example.avitotech.exception.ErrorCode;
import org.example.avitotech.model.PullRequest;
import org.example.avitotech.service.PullRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pullRequest")
public class PullRequestController {

    private final PullRequestService pullRequestService;

    public PullRequestController(PullRequestService pullRequestService) {
        this.pullRequestService = pullRequestService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPullRequest(@Valid @RequestBody PullRequestCreateRequest request) {
        log.info("Received request to create pull request: prId={}, prName={}, authorId={}, reviewers={}",
                request.getPrId(), request.getPrName(), request.getAuthorId(), request.getReviewers().size());

        try {
            PullRequest pullRequest = pullRequestService.createPullRequest(
                    request.getPrId(),
                    request.getPrName(),
                    request.getAuthorId(),
                    request.getReviewers()
            );

            log.info("Pull request created successfully: {}", request.getPrId());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of("pr", PullRequestResponse.from(pullRequest))
            );
        } catch (ApiException ex) {
            log.warn("Error creating pull request: {}", ex.getLogDescription());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while creating pull request", ex);
            throw new ApiException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Unexpected error during PR creation: " + ex.getMessage(),
                    ex
            );
        }
    }

    @PostMapping("/merge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> mergePullRequest(@Valid @RequestBody PullRequestMergeRequest request) {
        log.info("Received request to merge pull request: {}", request.getPrId());

        try {
            PullRequest mergedPullRequest = pullRequestService.mergePullRequest(request.getPrId());

            log.info("Pull request merged successfully: {}", request.getPrId());

            return ResponseEntity.ok(
                    Map.of("pr", PullRequestResponse.from(mergedPullRequest))
            );
        } catch (ApiException ex) {
            log.warn("Error merging pull request: {}", ex.getLogDescription());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while merging pull request", ex);
            throw new ApiException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Unexpected error during PR merge: " + ex.getMessage(),
                    ex
            );
        }
    }

    @PostMapping("/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reassignReviewer(@Valid @RequestBody PullRequestReassignRequest request) {
        log.info("Received request to reassign reviewer: prId={}, oldReviewer={}, newReviewer={}",
                request.getPrId(), request.getOldReviewerId(), request.getNewReviewerId());

        try {
            PullRequest updatedPullRequest = pullRequestService.reassignReviewer(
                    request.getPrId(),
                    request.getOldReviewerId(),
                    request.getNewReviewerId()
            );

            log.info("Reviewer reassigned successfully: {}", request.getPrId());

            return ResponseEntity.ok(
                    Map.of("pr", PullRequestResponse.from(updatedPullRequest))
            );
        } catch (ApiException ex) {
            log.warn("Error reassigning reviewer: {}", ex.getLogDescription());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while reassigning reviewer", ex);
            throw new ApiException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Unexpected error during reviewer reassignment: " + ex.getMessage(),
                    ex
            );
        }
    }
}
