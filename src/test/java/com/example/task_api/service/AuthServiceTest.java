package com.example.task_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.example.task_api.config.SecurityConfiguration;
import com.example.task_api.exception.EmailAlreadyRegisteredException;
import com.example.task_api.model.AppUser;
import com.example.task_api.repository.AppUserRepository;

import jakarta.persistence.EntityManager;


@DataJpaTest
@ActiveProfiles("test")
@Import({
        AuthService.class,
        SecurityConfiguration.class
})
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldNormalizeEmailAndHashPasswordWhenRegistering(){
        String rawPassword = "password123";

        AppUser registeredUser = authService.register(
                " Alice@Example.COM ",
                rawPassword
        );

        Long userId = registeredUser.getId();

        entityManager.clear();

        AppUser reloadedUser = appUserRepository.findById(userId)
                .orElseThrow();

         assertEquals(
                "alice@example.com",
                reloadedUser.getEmail()
        );

        assertNotEquals(
                rawPassword,
                reloadedUser.getPasswordHash()
        );

        assertTrue(
            passwordEncoder.matches(rawPassword, reloadedUser.getPasswordHash())
        );////保存的哈希确实对应原密码
    }

    @Test
    void shouldRejectDuplicateNormalizedEmail() {
        authService.register(
                "alice@example.com",
                "first-password");

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> authService.register(" ALICE@EXAMPLE.COM ", "second-password"));
        assertEquals(1L, appUserRepository.count());
    }
}
