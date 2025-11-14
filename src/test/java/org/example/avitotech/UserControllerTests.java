package org.example.avitotech;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.avitotech.dto.UserSetActiveRequest;
import org.example.avitotech.exception.ApiException;
import org.example.avitotech.exception.ErrorCode;
import org.example.avitotech.model.AssignedReviewer;
import org.example.avitotech.model.PullRequest;
import org.example.avitotech.model.PullRequestStatus;
import org.example.avitotech.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private List<PullRequest> testPullRequests;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых PR'ов
        testPullRequests = new ArrayList<>();

        AssignedReviewer reviewer1 = AssignedReviewer.builder()
                .userId("u1")
                .build();

        PullRequest pr1 = PullRequest.builder()
                .pullRequestId("pr1")
                .pullRequestName("Add authentication")
                .authorId("u5")
                .status(PullRequestStatus.OPEN)
                .createdAt(LocalDateTime.now().minusHours(2))
                .mergedAt(null)
                .assignedReviewers(List.of(reviewer1))
                .build();

        AssignedReviewer reviewer2 = AssignedReviewer.builder()
                .userId("u1")
                .build();

        AssignedReviewer reviewer3 = AssignedReviewer.builder()
                .userId("u2")
                .build();

        PullRequest pr2 = PullRequest.builder()
                .pullRequestId("pr2")
                .pullRequestName("Fix bug in payment")
                .authorId("u7")
                .status(PullRequestStatus.MERGED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .mergedAt(LocalDateTime.now().minusHours(1))
                .assignedReviewers(List.of(reviewer2, reviewer3))
                .build();

        testPullRequests.add(pr1);
        testPullRequests.add(pr2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSetUserActiveSuccess() throws Exception {
        UserSetActiveRequest request = UserSetActiveRequest.builder()
                .userId("u1")
                .isActive(true)
                .build();

        doNothing().when(userService).setUserActive("u1", true);

        mockMvc.perform(post("/users/setIsActive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User u1 status updated to true"));

        verify(userService, times(1)).setUserActive("u1", true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSetUserInactiveSuccess() throws Exception {
        UserSetActiveRequest request = UserSetActiveRequest.builder()
                .userId("u2")
                .isActive(false)
                .build();

        doNothing().when(userService).setUserActive("u2", false);

        mockMvc.perform(post("/users/setIsActive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User u2 status updated to false"));

        verify(userService, times(1)).setUserActive("u2", false);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSetUserActiveNotFound() throws Exception {
        UserSetActiveRequest request = UserSetActiveRequest.builder()
                .userId("nonexistent")
                .isActive(true)
                .build();

        doThrow(new ApiException(ErrorCode.NOT_FOUND, "User not found"))
                .when(userService).setUserActive("nonexistent", true);

        mockMvc.perform(post("/users/setIsActive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.message").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSetUserActiveForbidden() throws Exception {
        UserSetActiveRequest request = UserSetActiveRequest.builder()
                .userId("u1")
                .isActive(true)
                .build();

        mockMvc.perform(post("/users/setIsActive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSetUserActiveEmptyUserId() throws Exception {
        UserSetActiveRequest request = UserSetActiveRequest.builder()
                .userId("")
                .isActive(true)
                .build();

        mockMvc.perform(post("/users/setIsActive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSetUserActiveMissingIsActive() throws Exception {
        String requestBody = "{\"user_id\": \"u1\"}";

        mockMvc.perform(post("/users/setIsActive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSetUserActiveUnauthorized() throws Exception {
        UserSetActiveRequest request = UserSetActiveRequest.builder()
                .userId("u1")
                .isActive(true)
                .build();

        mockMvc.perform(post("/users/setIsActive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserReviewSuccessAdmin() throws Exception {
        when(userService.userExists("u1")).thenReturn(true);
        when(userService.getUserPullRequests("u1")).thenReturn(testPullRequests);

        mockMvc.perform(get("/users/getReview")
                        .param("user_id", "u1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests[0].pr_id").value("pr1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests[0].pr_name").value("Add authentication"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests[0].author_id").value("u5"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests[0].status").value("OPEN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests[0].reviewers").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests[1].pr_id").value("pr2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests[1].status").value("MERGED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests[1].merged_at").exists());

        verify(userService, times(1)).userExists("u1");
        verify(userService, times(1)).getUserPullRequests("u1");
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetUserReviewSuccessUser() throws Exception {
        when(userService.userExists("u1")).thenReturn(true);
        when(userService.getUserPullRequests("u1")).thenReturn(testPullRequests);

        mockMvc.perform(get("/users/getReview")
                        .param("user_id", "u1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserReviewEmptyList() throws Exception {
        when(userService.userExists("u3")).thenReturn(true);
        when(userService.getUserPullRequests("u3")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/users/getReview")
                        .param("user_id", "u3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pull_requests.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserReviewUserNotFound() throws Exception {
        when(userService.userExists("nonexistent")).thenReturn(false);

        mockMvc.perform(get("/users/getReview")
                        .param("user_id", "nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.message").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserReviewEmptyUserId() throws Exception {
        mockMvc.perform(get("/users/getReview")
                        .param("user_id", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserReviewMissingParameter() throws Exception {
        mockMvc.perform(get("/users/getReview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetUserReviewUnauthorized() throws Exception {
        mockMvc.perform(get("/users/getReview")
                        .param("user_id", "u1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
