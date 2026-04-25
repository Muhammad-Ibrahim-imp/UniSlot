package DBMS.UniSlot.Backend.config;



import  DBMS.UniSlot.Backend.entity.User;
import  DBMS.UniSlot.Backend.enums.Role;
import  DBMS.UniSlot.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer — Seeds essential data on application startup.
 *
 * On first run (empty database), this creates the default ADMIN account:
 *   Email:    admin@university.edu
 *   Password: admin123
 *
 * IMPORTANT: Change the admin password immediately after first login
 * in a production environment.
 *
 * Implements ApplicationRunner so it runs AFTER Spring context
 * is fully loaded (including JPA repositories).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createDefaultAdminIfAbsent();
    }

    private void createDefaultAdminIfAbsent() {
        String adminEmail = "admin@university.edu";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin account already exists: {}", adminEmail);
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);
        log.warn("=============================================================");
        log.warn(" DEFAULT ADMIN ACCOUNT CREATED");
        log.warn(" Email:    {}", adminEmail);
        log.warn(" Password: admin123");
        log.warn(" CHANGE THIS PASSWORD BEFORE GOING TO PRODUCTION!");
        log.warn("=============================================================");
    }
}
