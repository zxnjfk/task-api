package com.example.task_api.dto;

import java.time.LocalDateTime;

import com.example.task_api.model.Notification;
import com.example.task_api.model.NotificationStatus;
import com.example.task_api.model.NotificationType;

public class NotificationResponse {

    private Long id;
    private Long taskId;
    private NotificationType notificationType;
    private NotificationStatus status;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.taskId = notification.getTaskId();
        this.notificationType = notification.getNotificationType();
        this.status = notification.getStatus();
        this.content = notification.getContent();
        this.createdAt = notification.getCreatedAt();
        this.updatedAt = notification.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
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

}
