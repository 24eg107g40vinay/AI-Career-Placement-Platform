package com.example.career.config;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.career.model.Role;
import com.example.career.model.User;
import com.example.career.repository.UserRepository;

@Component
@ConditionalOnProperty(prefix = "app.admin-bootstrap", name = "enabled", havingValue = "true")
public class AdminAccountBootstrapRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(AdminAccountBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String name;
    private final String email;
    private final String password;

    public AdminAccountBootstrapRunner(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${app.admin-bootstrap.name:}") String name,
            @Value("${app.admin-bootstrap.email:}") String email,
            @Value("${app.admin-bootstrap.password:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String normalizedName = name == null ? "" : name.trim();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        validate(normalizedName, normalizedEmail, password);

        if (userRepository.countByRole(Role.ADMIN) > 0) {
            logger.info("Admin account bootstrap skipped: an administrator account already exists.");
            return;
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalStateException("Admin account bootstrap failed: the configured email is already registered.");
        }

        userRepository.save(new User(normalizedName, normalizedEmail, passwordEncoder.encode(password), Role.ADMIN));
        logger.info("Admin account bootstrap completed. Sign in using the configured email and password.");
    }

    private void validate(String normalizedName, String normalizedEmail, String configuredPassword) {
        if (!StringUtils.hasText(normalizedName) || normalizedName.length() > 120) {
            throw new IllegalStateException("Admin account bootstrap requires a name of 1 to 120 characters.");
        }
        if (!StringUtils.hasText(normalizedEmail) || normalizedEmail.length() > 190
                || !normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalStateException("Admin account bootstrap requires a valid email address.");
        }
        if (configuredPassword == null || configuredPassword.length() < 12 || configuredPassword.length() > 72
                || configuredPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Admin account bootstrap requires a password of at least 12 characters and at most 72 UTF-8 bytes.");
        }
    }
}
