package org.example.avitotech.config;

import org.example.avitotech.model.Team;
import org.example.avitotech.model.User;
import org.example.avitotech.repository.TeamRepository;
import org.example.avitotech.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataAutoInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataAutoInitializer.class);

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public DataAutoInitializer(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("🔨 Starting data initialization...");

        try {
            // 1. Создаём команду backend если нет
            if (!teamRepository.existsByTeamName("backend")) {
                Team backend = new Team();
                backend.setTeamName("backend");
                teamRepository.save(backend);
                logger.info("✅ Team 'backend' created");
            } else {
                logger.info("ℹ️ Team 'backend' already exists");
            }

            // 2. Добавляем пользователей
            addUserIfNotExists("u_admin", "Admin User", "backend");
            addUserIfNotExists("u_user", "Regular User", "backend");
            addUserIfNotExists("u1", "Reviewer 1", "backend");
            addUserIfNotExists("u2", "Reviewer 2", "backend");

            logger.info("✅ Data initialization completed successfully!");

        } catch (Exception e) {
            logger.error("❌ Error during data initialization", e);
            throw e;
        }
    }

    private void addUserIfNotExists(String userId, String username, String teamName) {
        if (!userRepository.existsByUserId(userId)) {
            User user = new User();
            user.setUserId(userId);
            user.setUsername(username);
            user.setTeamName(teamName);
            user.setIsActive(true);
            userRepository.save(user);
            logger.info("✅ User '{}' created", userId);
        } else {
            logger.info("ℹ️ User '{}' already exists", userId);
        }
    }
}
