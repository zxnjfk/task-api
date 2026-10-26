package com.example.task_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_api.dto.LoginRequest;
import com.example.task_api.dto.LoginResponse;
import com.example.task_api.dto.RegisterRequest;
import com.example.task_api.dto.UserResponse;
import com.example.task_api.model.AppUser;
import com.example.task_api.service.AuthService;
import com.example.task_api.service.JwtTokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthService authService,JwtTokenService jwtTokenService) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;

    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        AppUser user = authService.register(
                request.getEmail(),
                request.getPassword());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserResponse(user));
    }

    @PostMapping ("/login")
    public ResponseEntity<LoginResponse> login(
           @Valid @RequestBody LoginRequest request
    ) {
        AppUser user = authService.authenticate(
                request.getEmail(),
                request.getPassword()
        );

        String accessToken = jwtTokenService.issueToken(user);

        return ResponseEntity.ok(new LoginResponse(accessToken));

    }
}
