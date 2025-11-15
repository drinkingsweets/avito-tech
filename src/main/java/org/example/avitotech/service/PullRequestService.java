package org.example.avitotech.service;

import org.example.avitotech.exception.ApiException;
import org.example.avitotech.exception.ErrorCode;
import org.example.avitotech.model.AssignedReviewer;
import org.example.avitotech.model.PullRequest;
import org.example.avitotech.model.PullRequestStatus;
import org.example.avitotech.repository.PullRequestRepository;
import org.example.avitotech.repository.UserRepository;
import org.example.avitotech.repository.AssignedReviewerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PullRequestService {

    private final PullRequestRepository pullRequestRepository;
    private final UserRepository userRepository;
    private final AssignedReviewerRepository assignedReviewerRepository;

    @Autowired
    public PullRequestService(PullRequestRepository pullRequestRepository,
                              UserRepository userRepository,
                              AssignedReviewerRepository assignedReviewerRepository) {
        this.pullRequestRepository = pullRequestRepository;
        this.userRepository = userRepository;
        this.assignedReviewerRepository = assignedReviewerRepository;
    }

    @Transactional
    public PullRequest createPullRequest(String prId, String prName, String authorId, List<String> reviewerIds) {
        log.info("Creating pull request: prId={}, prName={}, authorId={}, reviewerIds={}",
                prId, prName, authorId, reviewerIds);

        if (pullRequestRepository.existsByPullRequestId(prId)) {
            log.warn("Pull request already exists: {}", prId);
            throw new ApiException(ErrorCode.ALREADY_EXISTS, "Pull request with ID " + prId + " already exists");
        }

        if (!userRepository.existsByUserId(authorId)) {
            log.warn("Author not found: {}", authorId);
            throw new ApiException(ErrorCode.NOT_FOUND, "Author not found");
        }

        for (String reviewerId : reviewerIds) {
            if (!userRepository.existsByUserId(reviewerId)) {
                log.warn("Reviewer not found: {}", reviewerId);
                throw new ApiException(ErrorCode.NOT_FOUND, "Reviewer not found: " + reviewerId);
            }
        }

        PullRequest pullRequest = PullRequest.builder()
                .pullRequestId(prId)
                .pullRequestName(prName)
                .authorId(authorId)
                .status(PullRequestStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .mergedAt(null)
                .build();

        PullRequest savedPullRequest = pullRequestRepository.save(pullRequest);
        log.info("Pull request created successfully: {}", prId);

        for (String reviewerId : reviewerIds) {
            AssignedReviewer assignedReviewer = AssignedReviewer.builder()
                    .pullRequest(savedPullRequest)
                    .userId(reviewerId)
                    .build();
            assignedReviewerRepository.save(assignedReviewer);
            log.debug("Assigned reviewer {} to pull request {}", reviewerId, prId);
        }

        log.info("Pull request created with {} reviewers: {}", reviewerIds.size(), prId);
        return pullRequestRepository.findByPullRequestId(prId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Pull request not found"));
    }

    @Transactional
    public PullRequest mergePullRequest(String prId) {
        log.info("Merging pull request: {}", prId);

        PullRequest pullRequest = pullRequestRepository.findByPullRequestId(prId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Pull request not found"));

        if (pullRequest.getStatus() != PullRequestStatus.OPEN) {
            log.warn("Cannot merge PR with status: {}", pullRequest.getStatus());
            throw new ApiException(ErrorCode.INVALID_STATE, "Pull request is not in OPEN status");
        }

        pullRequest.setStatus(PullRequestStatus.MERGED);
        pullRequest.setMergedAt(LocalDateTime.now());

        PullRequest mergedPullRequest = pullRequestRepository.save(pullRequest);
        log.info("Pull request merged successfully: {}", prId);

        return mergedPullRequest;
    }

    @Transactional
    public PullRequest reassignReviewer(String prId, String oldReviewerId, String newReviewerId) {
        log.info("Reassigning reviewer for PR: prId={}, oldReviewer={}, newReviewer={}",
                prId, oldReviewerId, newReviewerId);

        PullRequest pullRequest = pullRequestRepository.findByPullRequestId(prId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Pull request not found"));

        if (!userRepository.existsByUserId(newReviewerId)) {
            log.warn("New reviewer not found: {}", newReviewerId);
            throw new ApiException(ErrorCode.NOT_FOUND, "New reviewer not found");
        }

        AssignedReviewer oldReviewer = pullRequest.getAssignedReviewers().stream()
                .filter(ar -> ar.getUserId().equals(oldReviewerId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Reviewer {} not assigned to PR {}", oldReviewerId, prId);
                    return new ApiException(ErrorCode.NOT_FOUND, "Reviewer not assigned to this PR");
                });

        boolean newReviewerAlreadyAssigned = pullRequest.getAssignedReviewers().stream()
                .anyMatch(ar -> ar.getUserId().equals(newReviewerId));

        if (newReviewerAlreadyAssigned) {
            log.warn("Reviewer {} already assigned to PR {}", newReviewerId, prId);
            throw new ApiException(ErrorCode.ALREADY_EXISTS, "Reviewer already assigned to this PR");
        }

        assignedReviewerRepository.delete(oldReviewer);
        log.debug("Removed reviewer {} from PR {}", oldReviewerId, prId);

        AssignedReviewer newReviewer = AssignedReviewer.builder()
                .pullRequest(pullRequest)
                .userId(newReviewerId)
                .build();
        assignedReviewerRepository.save(newReviewer);
        log.info("Reassigned reviewer: old={}, new={}, PR={}", oldReviewerId, newReviewerId, prId);

        return pullRequestRepository.findByPullRequestId(prId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Pull request not found"));
    }

    @Transactional(readOnly = true)
    public PullRequest getPullRequestById(String prId) {
        log.debug("Fetching pull request by ID: {}", prId);
        return pullRequestRepository.findByPullRequestId(prId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Pull request not found"));
    }

    @Transactional(readOnly = true)
    public boolean prExists(String prId) {
        return pullRequestRepository.existsByPullRequestId(prId);
    }
}

