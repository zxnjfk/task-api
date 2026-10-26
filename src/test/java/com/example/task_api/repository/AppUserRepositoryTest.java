package com.example.task_api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.example.task_api.model.AppUser;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndFindUserByNormalizedEmail() {
        AppUser user = new AppUser(
                " Alice@Example.COM ",
                "encoded-password-hash");

        AppUser savedUser = appUserRepository.saveAndFlush(user);

        assertNotNull(savedUser.getId());

        entityManager.clear();

        AppUser reloadedUser = appUserRepository
                .findByEmail("alice@example.com")
                .orElseThrow();

        assertEquals("alice@example.com", reloadedUser.getEmail());
        assertEquals(
                "encoded-password-hash",
                reloadedUser.getPasswordHash());
        assertTrue(
                appUserRepository.existsByEmail("alice@example.com"));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        appUserRepository.saveAndFlush(
                new AppUser(
                        "alice@example.com",
                        "first-password-hash"));

        AppUser duplicateUser = new AppUser(
                "alice@example.com",
                "second-password-hash");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> appUserRepository.saveAndFlush(duplicateUser)
        );
    }

}
