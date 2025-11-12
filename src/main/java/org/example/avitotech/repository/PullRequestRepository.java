package org.example.avitotech.repository;

import org.example.avitotech.model.PullRequest;
import org.example.avitotech.model.PullRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, String> {
    Optional<PullRequest> findByPullRequestId(String pullRequestId);

    boolean existsByPullRequestId(String pullRequestId);

    List<PullRequest> findByAuthorIdAndStatus(String authorId, PullRequestStatus status);

    @Query("SELECT pr FROM PullRequest pr WHERE pr.authorId = :authorId AND pr.status = :status")
    List<PullRequest> findOpenPRsByAuthor(@Param("authorId") String authorId, @Param("status") PullRequestStatus status);

    @Query("SELECT DISTINCT pr FROM PullRequest pr " +
            "INNER JOIN pr.assignedReviewers ar " +
            "WHERE ar.userId = :userId")
    List<PullRequest> findByReviewerId(@Param("userId") String userId);

    @Query("SELECT DISTINCT pr FROM PullRequest pr " +
            "INNER JOIN pr.assignedReviewers ar " +
            "WHERE ar.userId = :userId AND pr.status = :status")
    List<PullRequest> findOpenPRsByReviewerId(@Param("userId") String userId, @Param("status") PullRequestStatus status);

    @Query("SELECT pr FROM PullRequest pr " +
            "INNER JOIN User u ON pr.authorId = u.userId " +
            "WHERE u.teamName = :teamName")
    List<PullRequest> findPRsByTeam(@Param("teamName") String teamName);
}
