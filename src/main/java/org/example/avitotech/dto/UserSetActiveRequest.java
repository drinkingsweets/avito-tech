package org.example.avitotech.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSetActiveRequest {

    @JsonProperty("user_id")
    @NotBlank(message = "user_id is required")
    private String userId;

    @JsonProperty("is_active")
    @NotNull(message = "is_active is required")
    private Boolean isActive;
}
