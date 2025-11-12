package org.example.avitotech.repository;

import org.example.avitotech.model.AssignedReviewer;
import org.example.avitotech.model.AssignedReviewerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignedReviewerRepository extends JpaRepository<AssignedReviewer, AssignedReviewerId> {
    List<AssignedReviewer> findByPullRequestId(String pullRequestId);

    List<AssignedReviewer> findByUserId(String userId);

    boolean existsByPullRequestIdAndUserId(String pullRequestId, String userId);

    Optional<AssignedReviewer> findByPullRequestIdAndUserId(String pullRequestId, String userId);

    long countByPullRequestId(String pullRequestId);

    void deleteByPullRequestId(String pullRequestId);

    void deleteByPullRequestIdAndUserId(String pullRequestId, String userId);

    @Query(value = "SELECT u.user_id, u.username, COUNT(ar.pull_request_id) as assignment_count " +
            "FROM users u " +
            "LEFT JOIN assigned_reviewers ar ON u.user_id = ar.user_id " +
            "GROUP BY u.user_id, u.username " +
            "ORDER BY assignment_count DESC", nativeQuery = true)
    List<Object[]> getAssignmentStatisticsByUser();

    @Query(value = "SELECT pr.pull_request_id, pr.pull_request_name, COUNT(ar.user_id) as reviewer_count " +
            "FROM pull_requests pr " +
            "LEFT JOIN assigned_reviewers ar ON pr.pull_request_id = ar.pull_request_id " +
            "GROUP BY pr.pull_request_id, pr.pull_request_name " +
            "ORDER BY reviewer_count DESC", nativeQuery = true)
    List<Object[]> getAssignmentStatisticsByPullRequest();

    @Query("SELECT ar FROM AssignedReviewer ar " +
            "INNER JOIN User u ON ar.userId = u.userId " +
            "WHERE u.teamName = :teamName")
    List<AssignedReviewer> findAssignmentsInTeam(@Param("teamName") String teamName);
}
