package com.example.task_api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.example.task_api.exception.InvalidTaskStateException;

@Entity
@Table(
        name = "tasks",
        indexes = @Index(
                name = "idx_tasks_deadline_status",
                columnList = "deadline, status"
        )
)
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private Long ownerId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Task() {

    }

    public Task(Long ownerId, String title, String description, Priority priority, LocalDate deadline) {
        validateOwnerId(ownerId);
        validateTitle(title);
        validatePriority(priority);
        validateDeadline(deadline);

        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.status = TaskStatus.TODO;
        this.priority = priority;
        this.deadline = deadline;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    private void validateOwnerId(Long ownerId) {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException(
                    "ownerId must be positive");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("任务标题不能为空");
        }
    }

    private void validatePriority(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("任务优先级不能为空");
        }
    }

    private void validateDeadline(LocalDate deadline) {
        if (deadline == null) {
            throw new IllegalArgumentException("任务截止日期不能为空");
        }
    }

    public Long getOwnerId(){
        return ownerId;
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public TaskStatus getStatus() {
        return this.status;
    }

    public Priority getPriority() {
        return this.priority;
    }

    public LocalDate getDeadline() {
        return this.deadline;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void start() {
        if (this.status != TaskStatus.TODO) {
            throw new InvalidTaskStateException(this.status, TaskStatus.IN_PROGRESS);
        }
        this.status = TaskStatus.IN_PROGRESS;
        refreshUpdatedAt();

    }

    public void complete() {
        if (this.status != TaskStatus.IN_PROGRESS) {
            throw new InvalidTaskStateException(this.status, TaskStatus.COMPLETED);
        }
        this.status = TaskStatus.COMPLETED;
        refreshUpdatedAt();
    }

    public void updateDetails(String newTitle, String newDescription) {
        validateTitle(newTitle);
        this.title = newTitle;
        this.description = newDescription;
        refreshUpdatedAt();
    }

    public void setPriority(Priority priority) {
        validatePriority(priority);
        this.priority = priority;
        refreshUpdatedAt();
    }

    public void setDeadline(LocalDate deadline) {
        validateDeadline(deadline);
        this.deadline = deadline;
        refreshUpdatedAt();
    }

    private void refreshUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

}
