package org.example.avitotech.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestCreateRequest {

    @JsonProperty("pr_id")
    @NotBlank(message = "pr_id is required")
    private String prId;

    @JsonProperty("pr_name")
    @NotBlank(message = "pr_name is required")
    private String prName;

    @JsonProperty("author_id")
    @NotBlank(message = "author_id is required")
    private String authorId;

    @JsonProperty("reviewers")
    @NotEmpty(message = "reviewers list cannot be empty")
    private List<String> reviewers;
}
