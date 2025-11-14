package org.example.avitotech.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.avitotech.model.Team;
import org.example.avitotech.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("members")
    private List<TeamMemberResponse> members;

    public static TeamResponse from(Team team) {
        return TeamResponse.builder()
                .teamName(team.getTeamName())
                .members(
                        team.getMembers() != null
                                ? team.getMembers().stream()
                                .map(TeamMemberResponse::from)
                                .collect(Collectors.toList())
                                : null
                )
                .build();
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamMemberResponse {

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("username")
        private String username;

        @JsonProperty("is_active")
        private Boolean isActive;

        public static TeamMemberResponse from(User user) {
            return TeamMemberResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .isActive(user.getIsActive())
                    .build();
        }
    }
}