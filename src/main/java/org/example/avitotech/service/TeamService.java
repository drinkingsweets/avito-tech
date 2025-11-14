package org.example.avitotech.service;

import lombok.extern.slf4j.Slf4j;
import org.example.avitotech.exception.ApiException;
import org.example.avitotech.exception.ErrorCode;
import org.example.avitotech.model.Team;
import org.example.avitotech.model.User;
import org.example.avitotech.repository.TeamRepository;
import org.example.avitotech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Team createTeam(Team team) {
        log.info("Creating team: {}", team.getTeamName());

        if (teamRepository.existsByTeamName(team.getTeamName())) {
            log.warn("Team already exists: {}", team.getTeamName());
            throw new ApiException(ErrorCode.TEAM_EXISTS);
        }

        if (team.getMembers() == null || team.getMembers().isEmpty()) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Team must have at least one member");
        }

        Team savedTeam = teamRepository.save(team);

        List<User> members = team.getMembers();
        for (User member : members) {
            member.setTeamName(savedTeam.getTeamName());
            userRepository.save(member);
        }

        return teamRepository.findByTeamNameWithMembers(savedTeam.getTeamName())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Team getTeamByName(String teamName) {
        log.debug("Fetching team: {}", teamName);

        if (teamName == null || teamName.trim().isEmpty()) {
            log.warn("Team name is empty or null");
            throw new ApiException(ErrorCode.NOT_FOUND, "Team name cannot be empty");
        }

        Team team = teamRepository.findByTeamNameWithMembers(teamName)
                .orElseThrow(() -> {
                    log.warn("Team not found: {}", teamName);
                    return new ApiException(ErrorCode.NOT_FOUND,
                            String.format("Team '%s' not found", teamName));
                });

        log.debug("Team fetched successfully: {} with {} members",
                teamName, team.getMembers().size());

        return team;
    }

    @Transactional(readOnly = true)
    public List<User> getActiveTeamMembers(String teamName) {
        log.debug("Fetching active members of team: {}", teamName);

        if (!teamRepository.existsByTeamName(teamName)) {
            log.warn("Team not found: {}", teamName);
            throw new ApiException(ErrorCode.NOT_FOUND,
                    String.format("Team '%s' not found", teamName));
        }

        List<User> activeMembers = userRepository.findActiveUsersByTeamName(teamName);
        log.debug("Found {} active members in team: {}", activeMembers.size(), teamName);

        return activeMembers;
    }

    @Transactional(readOnly = true)
    public List<User> getAllTeamMembers(String teamName) {
        log.debug("Fetching all members of team: {}", teamName);

        // Проверяем, что команда существует
        if (!teamRepository.existsByTeamName(teamName)) {
            log.warn("Team not found: {}", teamName);
            throw new ApiException(ErrorCode.NOT_FOUND,
                    String.format("Team '%s' not found", teamName));
        }

        List<User> members = userRepository.findByTeamName(teamName);
        log.debug("Found {} members in team: {}", members.size(), teamName);

        return members;
    }

    @Transactional(readOnly = true)
    public boolean teamExists(String teamName) {
        return teamRepository.existsByTeamName(teamName);
    }

    @Transactional(readOnly = true)
    public int getActiveTeamMembersCount(String teamName) {
        if (!teamRepository.existsByTeamName(teamName)) {
            throw new ApiException(ErrorCode.NOT_FOUND,
                    String.format("Team '%s' not found", teamName));
        }

        return (int) userRepository.findActiveUsersByTeamName(teamName).size();
    }

    @Transactional(readOnly = true)
    public List<User> getReviewersInTeam(String teamName) {
        log.debug("Fetching reviewers in team: {}", teamName);

        if (!teamRepository.existsByTeamName(teamName)) {
            throw new ApiException(ErrorCode.NOT_FOUND,
                    String.format("Team '%s' not found", teamName));
        }

        List<User> reviewers = userRepository.findReviewersInTeam(teamName);
        log.debug("Found {} reviewers in team: {}", reviewers.size(), teamName);

        return reviewers;
    }
}