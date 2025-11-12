package org.example.avitotech.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assigned_reviewers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(AssignedReviewerId.class)
public class AssignedReviewer {

    @Id
    @Column(name = "pull_request_id", nullable = false)
    private String pullRequestId;

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", referencedColumnName = "pull_request_id", insertable = false, updatable = false)
    private PullRequest pullRequest;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;
}
