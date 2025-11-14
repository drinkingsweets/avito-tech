package org.example.avitotech;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.avitotech.dto.TeamRequest;
import org.example.avitotech.exception.ApiException;
import org.example.avitotech.exception.ErrorCode;
import org.example.avitotech.model.Team;
import org.example.avitotech.model.User;
import org.example.avitotech.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamService teamService;

    private Team testTeam;
    private List<User> testMembers;

    @BeforeEach
    void setUp() {
        testMembers = new ArrayList<>();
        testMembers.add(User.builder()
                .userId("u1")
                .username("Alice")
                .teamName("backend")
                .isActive(true)
                .build());
        testMembers.add(User.builder()
                .userId("u2")
                .username("Bob")
                .teamName("backend")
                .isActive(true)
                .build());

        testTeam = Team.builder()
                .teamName("backend")
                .members(testMembers)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddTeamSuccess() throws Exception {
        // Arrange
        TeamRequest.TeamMemberRequest member1 = TeamRequest.TeamMemberRequest.builder()
                .userId("u1")
                .username("Alice")
                .isActive(true)
                .build();

        TeamRequest.TeamMemberRequest member2 = TeamRequest.TeamMemberRequest.builder()
                .userId("u2")
                .username("Bob")
                .isActive(true)
                .build();

        TeamRequest request = TeamRequest.builder()
                .teamName("backend")
                .members(List.of(member1, member2))
                .build();

        when(teamService.createTeam(any(Team.class))).thenReturn(testTeam);
        when(teamService.getTeamByName("backend")).thenReturn(testTeam);

        // Act & Assert
        mockMvc.perform(post("/team/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.team_name").value("backend"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.members").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.members[0].user_id").value("u1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.members[0].username").value("Alice"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.members[1].user_id").value("u2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.team.members[1].username").value("Bob"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAddTeamForbidden() throws Exception {
        // Arrange
        testTeam = Team.builder()
                .teamName("backend")
                .members(testMembers)
                .build();
        TeamRequest request = TeamRequest.builder()
                .teamName("backend")
                .members(List.of())
                .build();

        // Act & Assert
        mockMvc.perform(post("/team/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    /**
     * Тест: ошибка TEAM_EXISTS при попытке создать уже существующую команду
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddTeamAlreadyExists() throws Exception {
        Team team1 = Team.builder()
                .teamName("testtest")
                .build();

        TeamRequest request = TeamRequest.builder()
                .teamName("testtest")
                .members(List.of(
                        TeamRequest.TeamMemberRequest.builder()
                                .userId("u1")
                                .username("Alice")
                                .isActive(true)
                                .build()
                ))
                .build();

        when(teamService.createTeam(any(Team.class)))
                .thenThrow(new ApiException(ErrorCode.TEAM_EXISTS));



        mockMvc.perform(post("/team/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("TEAM_EXISTS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.message").exists());

        verify(teamService, times(1)).createTeam(any(Team.class));
    }


    /**
     * Тест: валидация - пустой список членов
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddTeamEmptyMembers() throws Exception {
        // Arrange
        TeamRequest request = TeamRequest.builder()
                .teamName("testtest")
                .members(new ArrayList<>())
                .build();

        // Act & Assert
        mockMvc.perform(post("/team/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    /**
     * Тест: валидация - пустое имя команды
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddTeamEmptyTeamName() throws Exception {
        // Arrange
        TeamRequest request = TeamRequest.builder()
                .teamName("")
                .members(List.of(
                        TeamRequest.TeamMemberRequest.builder()
                                .userId("u1")
                                .username("Alice")
                                .isActive(true)
                                .build()
                ))
                .build();

        // Act & Assert
        mockMvc.perform(post("/team/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    /**
     * Тест: отсутствие авторизации
     */
    @Test
    void testAddTeamUnauthorized() throws Exception {
        // Arrange
        TeamRequest request = TeamRequest.builder()
                .teamName("backend")
                .members(List.of())
                .build();

        // Act & Assert (без @WithMockUser - нет токена)
        mockMvc.perform(post("/team/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ===== Тесты для GET /team/get =====

    /**
     * Тест: успешное получение команды с ADMIN ролью
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetTeamSuccess() throws Exception {
        // Arrange
        when(teamService.getTeamByName("backend")).thenReturn(testTeam);

        // Act & Assert
        mockMvc.perform(get("/team/get")
                        .param("team_name", "backend")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.team_name").value("backend"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.members").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.members[0].user_id").value("u1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.members[0].is_active").value(true));
    }

    /**
     * Тест: успешное получение команды с USER ролью
     */
    @Test
    @WithMockUser(roles = "USER")
    void testGetTeamWithUserRole() throws Exception {
        // Arrange
        when(teamService.getTeamByName("backend")).thenReturn(testTeam);

        // Act & Assert
        mockMvc.perform(get("/team/get")
                        .param("team_name", "backend")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.team_name").value("backend"));
    }

    /**
     * Тест: ошибка NOT_FOUND при получении несуществующей команды
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetTeamNotFound() throws Exception {
        // Arrange
        when(teamService.getTeamByName("nonexistent"))
                .thenThrow(new ApiException(ErrorCode.NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/team/get")
                        .param("team_name", "nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.message").exists());
    }

    /**
     * Тест: пустой параметр team_name
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetTeamEmptyTeamName() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/team/get")
                        .param("team_name", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Тест: отсутствие параметра team_name
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetTeamMissingParameter() throws Exception {
        // Act & Assert (без параметра team_name)
        mockMvc.perform(get("/team/get")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Тест: отсутствие авторизации для GET
     */
    @Test
    void testGetTeamUnauthorized() throws Exception {
        // Act & Assert (без @WithMockUser - нет токена)
        mockMvc.perform(get("/team/get")
                        .param("team_name", "backend")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Тест: GET с неактивными участниками
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetTeamWithInactiveMembers() throws Exception {
        // Arrange
        List<User> mixedMembers = new ArrayList<>();
        mixedMembers.add(User.builder()
                .userId("u1")
                .username("Alice")
                .teamName("backend")
                .isActive(true)
                .build());
        mixedMembers.add(User.builder()
                .userId("u2")
                .username("Bob")
                .teamName("backend")
                .isActive(false)  // неактивный
                .build());

        Team teamWithInactive = Team.builder()
                .teamName("backend")
                .members(mixedMembers)
                .build();

        when(teamService.getTeamByName("backend")).thenReturn(teamWithInactive);

        // Act & Assert
        mockMvc.perform(get("/team/get")
                        .param("team_name", "backend")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.members[0].is_active").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.members[1].is_active").value(false));
    }
}

