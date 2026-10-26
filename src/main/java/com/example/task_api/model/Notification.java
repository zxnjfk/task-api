package com.example.task_api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "notifications", uniqueConstraints = @UniqueConstraint(name = "uk_notifications_async_job_id", columnNames = "async_job_id"))

public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false, updatable = false)
    private Long recipientId;

    @Column(name = "task_id", nullable = false, updatable = false)
    private Long taskId;

    @Column(name = "async_job_id", nullable = false, updatable = false)
    private Long asyncJobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50, updatable = false)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT", updatable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Notification() {

    }

    public Notification(
            Long recipientId,
            Long taskId,
            Long asyncJobId,
            NotificationType notificationType,
            String content

    ) {
        if (recipientId == null || recipientId <= 0) {
            throw new IllegalArgumentException(
                    "recipientId must be positive");
        }

        if (taskId == null || taskId <= 0) {
            throw new IllegalArgumentException(
                    "taskId must be positive");
        }
        if (asyncJobId == null || asyncJobId <= 0) {
            throw new IllegalArgumentException(
                    "asyncJobId must be positive");
        }
        if (notificationType == null) {
            throw new IllegalArgumentException(
                    "notificationType must not be null");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "content must not be blank");
        }
        this.recipientId = recipientId;
        this.taskId = taskId;
        this.asyncJobId = asyncJobId;
        this.notificationType = notificationType;
        this.status = NotificationStatus.UNREAD;
        this.content = content;

        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

    }

    public void markAsRead() {
        if(this.status == NotificationStatus.READ) {
            return;
        }

        this.status = NotificationStatus.READ;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getAsyncJobId() {
        return asyncJobId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getContent() {
        return content;
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
