package org.example.avitotech.repository;

import org.example.avitotech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserId(String userId);

    List<User> findByTeamName(String teamName);

    @Query("SELECT u FROM User u WHERE u.teamName = :teamName AND u.isActive = true")
    List<User> findActiveUsersByTeamName(@Param("teamName") String teamName);

    @Query("SELECT u FROM User u WHERE u.teamName = :teamName AND u.isActive = true AND u.userId != :excludeUserId")
    List<User> findActiveUsersExcludingAuthor(@Param("teamName") String teamName, @Param("excludeUserId") String excludeUserId);

    boolean existsByUserId(String userId);

    @Query(value = "SELECT DISTINCT u.* FROM users u " +
            "INNER JOIN assigned_reviewers ar ON u.user_id = ar.user_id " +
            "WHERE u.team_name = :teamName", nativeQuery = true)
    List<User> findReviewersInTeam(@Param("teamName") String teamName);
}
