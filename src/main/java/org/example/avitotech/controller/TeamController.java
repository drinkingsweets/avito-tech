package org.example.avitotech.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.avitotech.dto.TeamRequest;
import org.example.avitotech.dto.TeamResponse;
import org.example.avitotech.exception.ApiException;
import org.example.avitotech.exception.ErrorCode;
import org.example.avitotech.model.Team;
import org.example.avitotech.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/team")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addTeam(@RequestBody @Valid TeamRequest teamRequest) {
        log.info("Received request to create team: {}", teamRequest.getTeamName());

        try {
            Team team = teamRequest.toEntity();
            Team savedTeam = teamService.createTeam(team);
            TeamResponse response = TeamResponse.from(savedTeam);

            log.info("Team created successfully: {}", savedTeam.getTeamName());

            return new ResponseEntity<>(
                    Map.of("team", response),
                    HttpStatus.CREATED
            );
        } catch (ApiException ex) {
            log.warn("Error creating team: {}", ex.getLogDescription());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while creating team", ex);
            throw new ApiException(
                    ErrorCode.NOT_FOUND,
                    "Unexpected error during team creation: " + ex.getMessage(),
                    ex
            );
        }
    }

    @GetMapping("/get")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getTeam(
            @RequestParam(name = "team_name") String teamName) {

        log.debug("Received request to get team: {}", teamName);

        try {
            if (teamName == null || teamName.trim().isEmpty()) {
                log.warn("Team name parameter is empty");
                throw new ApiException(
                        ErrorCode.NOT_FOUND,
                        "Team name cannot be empty", ErrorCode.ErrorCategory.CONFLICT);
            }

            Team team = teamService.getTeamByName(teamName);
            TeamResponse response = TeamResponse.from(team);
            log.debug("Team retrieved successfully: {}", teamName);

            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            log.warn("Error retrieving team '{}': {}", teamName, ex.getLogDescription());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while retrieving team '{}'", teamName, ex);
            throw new ApiException(
                    ErrorCode.NOT_FOUND,
                    "Unexpected error during team retrieval: " + ex.getMessage(),
                    ex);
        }
    }
}

