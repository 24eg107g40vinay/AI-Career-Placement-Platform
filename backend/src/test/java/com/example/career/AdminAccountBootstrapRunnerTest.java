package com.example.career;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.career.config.AdminAccountBootstrapRunner;
import com.example.career.model.Role;
import com.example.career.model.User;
import com.example.career.repository.UserRepository;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class AdminAccountBootstrapRunnerTest {
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);

    @Test
    void createsHashedFirstAdminWithNormalizedEmail() throws Exception {
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(0L);
        when(userRepository.existsByEmailIgnoreCase("owner@example.test")).thenReturn(false);
        when(passwordEncoder.encode("Correct-Horse-123")).thenReturn("bcrypt-hash");

        AdminAccountBootstrapRunner runner = new AdminAccountBootstrapRunner(
                userRepository, passwordEncoder, " Platform Owner ", " OWNER@EXAMPLE.TEST ", "Correct-Horse-123");
        runner.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("Platform Owner", userCaptor.getValue().getName());
        assertEquals("owner@example.test", userCaptor.getValue().getEmail());
        assertEquals(Role.ADMIN, userCaptor.getValue().getRole());
        assertEquals("bcrypt-hash", userCaptor.getValue().getPassword());
    }

    @Test
    void doesNotCreateAnotherAdminWhenOneAlreadyExists() throws Exception {
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        AdminAccountBootstrapRunner runner = new AdminAccountBootstrapRunner(
                userRepository, passwordEncoder, "Platform Owner", "owner@example.test", "Correct-Horse-123");
        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refusesToUseAnEmailAlreadyRegisteredToANonAdmin() {
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(0L);
        when(userRepository.existsByEmailIgnoreCase("owner@example.test")).thenReturn(true);

        AdminAccountBootstrapRunner runner = new AdminAccountBootstrapRunner(
                userRepository, passwordEncoder, "Platform Owner", "owner@example.test", "Correct-Horse-123");

        assertThrows(IllegalStateException.class, () -> runner.run(new DefaultApplicationArguments(new String[0])));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsWeakBootstrapPassword() {
        AdminAccountBootstrapRunner runner = new AdminAccountBootstrapRunner(
                userRepository, passwordEncoder, "Platform Owner", "owner@example.test", "short");

        assertThrows(IllegalStateException.class, () -> runner.run(new DefaultApplicationArguments(new String[0])));
        verify(userRepository, never()).save(any(User.class));
    }
}
