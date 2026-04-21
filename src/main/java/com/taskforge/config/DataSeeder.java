package com.taskforge.config;

import com.taskforge.entity.Task;
import com.taskforge.entity.User;
import com.taskforge.repository.TaskRepository;
import com.taskforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

// @Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            // Only seed if no users exist
            if (userRepository.count() > 0) {
                log.info("Database already seeded — skipping.");
                return;
            }

            log.info("Seeding demo data...");

            // Create demo user
            User user = userRepository.save(User.builder()
                .name("Demo User")
                .email("user@demo.com")
                .password(passwordEncoder.encode("password123"))
                .role(User.Role.USER)
                .build());

            // Create admin
            User admin = userRepository.save(User.builder()
                .name("Admin")
                .email("admin@demo.com")
                .password(passwordEncoder.encode("admin123"))
                .role(User.Role.ADMIN)
                .build());

            // Seed sample tasks for demo user
            taskRepository.saveAll(List.of(
                Task.builder()
                    .title("Set up CI/CD pipeline")
                    .description("Configure GitHub Actions for automated testing and deployment to staging and production environments.")
                    .priority(Task.Priority.HIGH)
                    .status(Task.Status.IN_PROGRESS)
                    .deadline(LocalDateTime.now().plusHours(20))
                    .user(user)
                    .build(),

                Task.builder()
                    .title("Design database schema")
                    .description("Create ERD and finalize table structures for the new reporting module.")
                    .priority(Task.Priority.MEDIUM)
                    .status(Task.Status.TODO)
                    .deadline(LocalDateTime.now().plusDays(2))
                    .user(user)
                    .build(),

                Task.builder()
                    .title("Write unit tests for AuthService")
                    .description("Cover registration, login, token generation and validation edge cases.")
                    .priority(Task.Priority.MEDIUM)
                    .status(Task.Status.TODO)
                    .deadline(LocalDateTime.now().plusDays(4))
                    .user(user)
                    .build(),

                Task.builder()
                    .title("Update API documentation")
                    .description("Sync Swagger/OpenAPI docs with latest endpoint changes.")
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.DONE)
                    .deadline(LocalDateTime.now().minusDays(1))
                    .user(user)
                    .build(),

                Task.builder()
                    .title("Fix login redirect bug")
                    .description("Users are redirected to 404 after successful login from the email link.")
                    .priority(Task.Priority.HIGH)
                    .status(Task.Status.TODO)
                    .deadline(LocalDateTime.now().plusHours(8))
                    .user(user)
                    .build(),

                Task.builder()
                    .title("Refactor task repository queries")
                    .description("Optimize the paginated filter queries to reduce N+1 issues.")
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.TODO)
                    .deadline(LocalDateTime.now().plusDays(10))
                    .user(user)
                    .build()
            ));

            log.info("✅ Demo data seeded successfully.");
            log.info("   user@demo.com  / password123  (USER)");
            log.info("   admin@demo.com / admin123     (ADMIN)");
        };
    }
}
