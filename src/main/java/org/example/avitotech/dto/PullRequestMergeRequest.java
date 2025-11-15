package org.example.avitotech.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestMergeRequest {

    @JsonProperty("pr_id")
    @NotBlank(message = "pr_id is required")
    private String prId;
}
