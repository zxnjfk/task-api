package com.example.task_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.task_api.config.SecurityConfiguration;
import com.example.task_api.model.AppUser;
import com.example.task_api.repository.AppUserRepository;


@DataJpaTest
@ActiveProfiles ("test")
@Import({
        JwtTokenService.class,
        SecurityConfiguration.class
})

class JwtTokenServiceTest {

    private static final Instant ISSUED_AT =
            Instant.parse("2030-01-01T01:00:00Z");

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private AppUserRepository appUserRepository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUpClock(){
        when(clock.instant()).thenReturn(ISSUED_AT);
    }

    @Test
    void shouldIssueValidTokenForSavedUser() {
        AppUser user = appUserRepository.saveAndFlush(
                new AppUser(
                        "alice@example.com",
                        "encoded-password-hash"
                )
        );

        String tokenValue = jwtTokenService.issueToken(user);

        Jwt jwt = jwtDecoder.decode(tokenValue);

        assertNotNull(tokenValue);
        assertEquals(user.getId().toString(), jwt.getSubject());
        assertEquals("task-api", jwt.getClaimAsString("iss"));
        assertEquals(ISSUED_AT, jwt.getIssuedAt());
        assertEquals(ISSUED_AT.plus(Duration.ofHours(2)), jwt.getExpiresAt());

    }
}
