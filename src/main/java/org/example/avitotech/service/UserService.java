package org.example.avitotech.service;

import org.example.avitotech.exception.ApiException;
import org.example.avitotech.exception.ErrorCode;
import org.example.avitotech.model.PullRequest;
import org.example.avitotech.model.User;
import org.example.avitotech.repository.UserRepository;
import org.example.avitotech.repository.PullRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PullRequestRepository pullRequestRepository;

    @Autowired
    public UserService(UserRepository userRepository, PullRequestRepository pullRequestRepository) {
        this.userRepository = userRepository;
        this.pullRequestRepository = pullRequestRepository;
    }

    @Transactional
    public void setUserActive(String userId, Boolean isActive) {
        log.info("Setting user {} active status to {}", userId, isActive);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found", ErrorCode.ErrorCategory.CONFLICT));

        user.setIsActive(isActive);
        userRepository.save(user);

        log.info("User {} active status updated to {}", userId, isActive);
    }

    @Transactional(readOnly = true)
    public boolean userExists(String userId) {
        return userRepository.existsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<PullRequest> getUserPullRequests(String userId) {
        log.debug("Fetching pull requests for reviewer: {}", userId);

        List<PullRequest> pullRequests = pullRequestRepository.findByReviewerId(userId);

        log.debug("Found {} pull requests for reviewer: {}", pullRequests.size(), userId);

        return pullRequests;
    }
}
