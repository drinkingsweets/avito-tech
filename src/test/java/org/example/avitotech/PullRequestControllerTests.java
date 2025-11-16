package org.example.avitotech;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.avitotech.dto.PullRequestCreateRequest;
import org.example.avitotech.dto.PullRequestMergeRequest;
import org.example.avitotech.dto.PullRequestReassignRequest;
import org.example.avitotech.exception.ApiException;
import org.example.avitotech.exception.ErrorCode;
import org.example.avitotech.model.AssignedReviewer;
import org.example.avitotech.model.PullRequest;
import org.example.avitotech.model.PullRequestStatus;
import org.example.avitotech.service.PullRequestService;
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
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PullRequestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PullRequestService pullRequestService;

    private PullRequest testPullRequest;

    @BeforeEach
    void setUp() {
        testPullRequest = PullRequest.builder()
                .pullRequestId("pr123")
                .pullRequestName("Add authentication")
                .authorId("u5")
                .status(PullRequestStatus.OPEN)
                .createdAt(LocalDateTime.now().minusHours(2))
                .mergedAt(null)
                .assignedReviewers(List.of(
                        AssignedReviewer.builder().userId("u1").build(),
                        AssignedReviewer.builder().userId("u2").build(),
                        AssignedReviewer.builder().userId("u3").build()
                ))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreatePullRequestSuccess() throws Exception {
        PullRequestCreateRequest request = PullRequestCreateRequest.builder()
                .prId("pr123")
                .prName("Add authentication")
                .authorId("u5")
                .reviewers(List.of("u1", "u2", "u3"))
                .build();

        when(pullRequestService.createPullRequest(
                "pr123", "Add authentication", "u5", List.of("u1", "u2", "u3")))
                .thenReturn(testPullRequest);

        mockMvc.perform(post("/pullRequest/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pr.pr_id").value("pr123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pr.pr_name").value("Add authentication"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pr.author_id").value("u5"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pr.status").value("OPEN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pr.reviewers.length()").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pr.merged_at").doesNotExist());

        verify(pullRequestService, times(1)).createPullRequest(
                "pr123", "Add authentication", "u5", List.of("u1", "u2", "u3"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreatePullRequestAlreadyExists() throws Exception {
        PullRequestCreateRequest request = PullRequestCreateRequest.builder()
                .prId("pr123")
                .prName("Add authentication")
                .authorId("u5")
                .reviewers(List.of("u1", "u2", "u3"))
                .build();

        doThrow(new ApiException(ErrorCode.ALREADY_EXISTS, "Pull request with ID pr123 already exists", ErrorCode.ErrorCategory.CONFLICT))
                .when(pullRequestService).createPullRequest(
                        "pr123", "Add authentication", "u5", List.of("u1", "u2", "u3"));

        mockMvc.perform(post("/pullRequest/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("ALREADY_EXISTS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.message").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreatePullRequestAuthorNotFound() throws Exception {
        PullRequestCreateRequest request = PullRequestCreateRequest.builder()
                .prId("pr123")
                .prName("Add authentication")
                .authorId("nonexistent")
                .reviewers(List.of("u1", "u2", "u3"))
                .build();

        doThrow(new ApiException(ErrorCode.NOT_FOUND, "Author not found", ErrorCode.ErrorCategory.CONFLICT))
                .when(pullRequestService).createPullRequest(
                        "pr123", "Add authentication", "nonexistent", List.of("u1", "u2", "u3"));

        mockMvc.perform(post("/pullRequest/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreatePullRequestReviewerNotFound() throws Exception {
        PullRequestCreateRequest request = PullRequestCreateRequest.builder()
                .prId("pr123")
                .prName("Add authentication")
                .authorId("u5")
                .reviewers(List.of("u1", "nonexistent"))
                .build();

        doThrow(new ApiException(ErrorCode.NOT_FOUND, "Reviewer not found: nonexistent", ErrorCode.ErrorCategory.CONFLICT))
                .when(pullRequestService).createPullRequest(
                        "pr123", "Add authentication", "u5", List.of("u1", "nonexistent"));

        mockMvc.perform(post("/pullRequest/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreatePullRequestForbidden() throws Exception {
        PullRequestCreateRequest request = PullRequestCreateRequest.builder()
                .prId("pr123")
                .prName("Add authentication")
                .authorId("u5")
                .reviewers(List.of("u1", "u2", "u3"))
                .build();

        mockMvc.perform(post("/pullRequest/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreatePullRequestEmptyReviewers() throws Exception {
        PullRequestCreateRequest request = PullRequestCreateRequest.builder()
                .prId("pr123")
                .prName("Add authentication")
                .authorId("u5")
                .reviewers(List.of())
                .build();

        mockMvc.perform(post("/pullRequest/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreatePullRequestMissingPrId() throws Exception {
        String requestBody = "{\"pr_name\":\"Add auth\",\"author_id\":\"u5\",\"reviewers\":[\"u1\"]}";

        mockMvc.perform(post("/pullRequest/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreatePullRequestUnauthorized() throws Exception {
        PullRequestCreateRequest request = PullRequestCreateRequest.builder()
                .prId("pr123")
                .prName("Add authentication")
                .authorId("u5")
                .reviewers(List.of("u1", "u2", "u3"))
                .build();

        mockMvc.perform(post("/pullRequest/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMergePullRequestSuccess() throws Exception {
        PullRequestMergeRequest request = PullRequestMergeRequest.builder()
                .prId("pr123")
                .build();

        PullRequest mergedPR = testPullRequest;
        mergedPR.setStatus(PullRequestStatus.MERGED);
        mergedPR.setMergedAt(LocalDateTime.now());

        when(pullRequestService.mergePullRequest("pr123")).thenReturn(mergedPR);

        mockMvc.perform(post("/pullRequest/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pr.pr_id").value("pr123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pr.status").value("MERGED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pr.merged_at").exists());

        verify(pullRequestService, times(1)).mergePullRequest("pr123");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMergePullRequestNotFound() throws Exception {
        PullRequestMergeRequest request = PullRequestMergeRequest.builder()
                .prId("nonexistent")
                .build();

        doThrow(new ApiException(ErrorCode.NOT_FOUND, "Pull request not found", ErrorCode.ErrorCategory.CONFLICT))
                .when(pullRequestService).mergePullRequest("nonexistent");

        mockMvc.perform(post("/pullRequest/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMergePullRequestInvalidState() throws Exception {
        PullRequestMergeRequest request = PullRequestMergeRequest.builder()
                .prId("pr123")
                .build();

        doThrow(new ApiException(ErrorCode.INVALID_STATE, "Pull request is not in OPEN status", ErrorCode.ErrorCategory.CONFLICT))
                .when(pullRequestService).mergePullRequest("pr123");

        mockMvc.perform(post("/pullRequest/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("INVALID_STATE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testMergePullRequestForbidden() throws Exception {
        PullRequestMergeRequest request = PullRequestMergeRequest.builder()
                .prId("pr123")
                .build();

        mockMvc.perform(post("/pullRequest/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMergePullRequestEmptyPrId() throws Exception {
        PullRequestMergeRequest request = PullRequestMergeRequest.builder()
                .prId("")
                .build();

        mockMvc.perform(post("/pullRequest/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReassignReviewerSuccess() throws Exception {
        PullRequestReassignRequest request = PullRequestReassignRequest.builder()
                .prId("pr123")
                .oldReviewerId("u1")
                .newReviewerId("u4")
                .build();

        PullRequest updatedPR = testPullRequest;
        updatedPR.setAssignedReviewers(List.of(
                AssignedReviewer.builder().userId("u4").build(),
                AssignedReviewer.builder().userId("u2").build(),
                AssignedReviewer.builder().userId("u3").build()
        ));

        when(pullRequestService.reassignReviewer("pr123", "u1", "u4")).thenReturn(updatedPR);

        mockMvc.perform(post("/pullRequest/reassign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pr.pr_id").value("pr123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pr.reviewers.length()").value(3));

        verify(pullRequestService, times(1)).reassignReviewer("pr123", "u1", "u4");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReassignReviewerPrNotFound() throws Exception {
        PullRequestReassignRequest request = PullRequestReassignRequest.builder()
                .prId("nonexistent")
                .oldReviewerId("u1")
                .newReviewerId("u4")
                .build();

        doThrow(new ApiException(ErrorCode.NOT_FOUND, "Pull request not found", ErrorCode.ErrorCategory.CONFLICT))
                .when(pullRequestService).reassignReviewer("nonexistent", "u1", "u4");

        mockMvc.perform(post("/pullRequest/reassign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReassignReviewerOldReviewerNotAssigned() throws Exception {
        PullRequestReassignRequest request = PullRequestReassignRequest.builder()
                .prId("pr123")
                .oldReviewerId("nonexistent")
                .newReviewerId("u4")
                .build();

        doThrow(new ApiException(ErrorCode.NOT_FOUND, "Reviewer not assigned to this PR", ErrorCode.ErrorCategory.CONFLICT))
                .when(pullRequestService).reassignReviewer("pr123", "nonexistent", "u4");

        mockMvc.perform(post("/pullRequest/reassign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReassignReviewerAlreadyAssigned() throws Exception {
        PullRequestReassignRequest request = PullRequestReassignRequest.builder()
                .prId("pr123")
                .oldReviewerId("u1")
                .newReviewerId("u2")
                .build();

        doThrow(new ApiException(ErrorCode.ALREADY_EXISTS, "Reviewer already assigned to this PR", ErrorCode.ErrorCategory.CONFLICT))
                .when(pullRequestService).reassignReviewer("pr123", "u1", "u2");

        mockMvc.perform(post("/pullRequest/reassign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("ALREADY_EXISTS"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testReassignReviewerForbidden() throws Exception {
        PullRequestReassignRequest request = PullRequestReassignRequest.builder()
                .prId("pr123")
                .oldReviewerId("u1")
                .newReviewerId("u4")
                .build();

        mockMvc.perform(post("/pullRequest/reassign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReassignReviewerEmptyOldId() throws Exception {
        PullRequestReassignRequest request = PullRequestReassignRequest.builder()
                .prId("pr123")
                .oldReviewerId("")
                .newReviewerId("u4")
                .build();

        mockMvc.perform(post("/pullRequest/reassign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}

