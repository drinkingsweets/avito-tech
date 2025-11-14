package org.example.avitotech.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.avitotech.model.Team;
import org.example.avitotech.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRequest {

    @JsonProperty("team_name")
    @NotBlank(message = "team_name is required")
    private String teamName;

    @JsonProperty("members")
    @NotEmpty(message = "members list cannot be empty")
    @Valid
    private List<TeamMemberRequest> members;

    public Team toEntity() {
        List<User> users = new ArrayList<>();

        if (this.members != null) {
            users = this.members.stream()
                    .map(member -> member.toEntity(this.teamName))
                    .collect(Collectors.toList());
        }

        return Team.builder()
                .teamName(this.teamName)
                .members(users)
                .build();
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamMemberRequest {

        @JsonProperty("user_id")
        @NotBlank(message = "user_id is required")
        private String userId;

        @JsonProperty("username")
        @NotBlank(message = "username is required")
        private String username;

        @JsonProperty("is_active")
        @NotNull(message = "is_active is required")
        private Boolean isActive;

        public User toEntity(String teamName) {
            return User.builder()
                    .userId(this.userId)
                    .username(this.username)
                    .isActive(this.isActive)
                    .teamName(teamName)
                    .build();
        }
    }
}
