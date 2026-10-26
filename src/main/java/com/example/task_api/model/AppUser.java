package com.example.task_api.model;

import java.time.LocalDateTime;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
       name = "app_users",
       uniqueConstraints = @UniqueConstraint (
              name = "uk_app_users_email",
              columnNames = "email"
       )
)
public class AppUser {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false,length = 255)
    private String passwordHash;

    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column (nullable = false)
    private Long version;

    protected AppUser(){

    }

    public AppUser(String email, String passwordHash){
        this.email = normalizeEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);

        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static String normalizeEmail(String email){
        if(email == null || email.isBlank()){
            throw new IllegalArgumentException("email must not be blank");
        }

        return email.trim().toLowerCase(Locale.ROOT);

    }

    private String validatePasswordHash(String passwordHash){
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "passwordHash must not be blank"
            );
        }

        return passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

}
