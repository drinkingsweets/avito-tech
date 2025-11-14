package org.example.avitotech.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.avitotech.model.AssignedReviewer;
import org.example.avitotech.model.PullRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestResponse {

    @JsonProperty("pr_id")
    private String prId;

    @JsonProperty("pr_name")
    private String prName;

    @JsonProperty("author_id")
    private String authorId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("reviewers")
    private List<String> reviewers;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("merged_at")
    private LocalDateTime mergedAt;

    public static PullRequestResponse from(PullRequest pr) {
        List<String> reviewerIds = pr.getAssignedReviewers() != null
                ? pr.getAssignedReviewers().stream()
                .map(AssignedReviewer::getUserId)
                .collect(Collectors.toList())
                : List.of();

        return PullRequestResponse.builder()
                .prId(pr.getPullRequestId())
                .prName(pr.getPullRequestName())
                .authorId(pr.getAuthorId())
                .status(pr.getStatus().name())
                .reviewers(reviewerIds)
                .createdAt(pr.getCreatedAt())
                .mergedAt(pr.getMergedAt())
                .build();
    }
}
