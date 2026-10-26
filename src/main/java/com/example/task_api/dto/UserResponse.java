package com.example.task_api.dto;

import java.time.LocalDateTime;

import com.example.task_api.model.AppUser;

public class UserResponse {

    private final Long id;
    private final String email;
    private final LocalDateTime createdAt;

    public UserResponse(AppUser user){
        this.id = user.getId();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
