package org.example.avitotech.repository;

import org.example.avitotech.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
    Optional<Team> findByTeamName(String teamName);

    boolean existsByTeamName(String teamName);

    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.members WHERE t.teamName = :teamName")
    Optional<Team> findByTeamNameWithMembers(@Param("teamName") String teamName);
}