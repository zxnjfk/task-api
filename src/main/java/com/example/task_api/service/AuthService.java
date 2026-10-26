package com.example.task_api.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.task_api.exception.EmailAlreadyRegisteredException;
import com.example.task_api.model.AppUser;
import com.example.task_api.repository.AppUserRepository;
import com.example.task_api.exception.InvalidCredentialsException;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(String email, String rawPassword) {
        String normalizedEmail = AppUser.normalizeEmail(email);

        validateRegistrationPassword(rawPassword);

        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException();
        }

        String passwordHash = passwordEncoder.encode(rawPassword);

        AppUser user = new AppUser(normalizedEmail, passwordHash);

        try {
            return appUserRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyRegisteredException();
        }

    }

    @Transactional(readOnly = true)
    public AppUser authenticate(String email, String rawPassword) {
        String normalizedEmail = AppUser.normalizeEmail(email);

        AppUser user = appUserRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (rawPassword == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }

    private void validateRegistrationPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "rawPassword must not be blank");
        }

        if (rawPassword.length() < 8
                || rawPassword.length() > 72) {
            throw new IllegalArgumentException(
                    "rawPassword length must be between 8 and 72");
        }
    }

}
