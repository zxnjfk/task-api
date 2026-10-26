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

@Entity
@Table(name="task_events")
public class TaskEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id",nullable=false)
    private  Long taskId;

    @Enumerated (EnumType.STRING)
    @Column(name = "from_status",nullable = false,length = 20)
    private TaskStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status",nullable = false,length = 20)
    private TaskStatus toStatus;

    @Enumerated (EnumType.STRING)
    @Column (name="event_type",nullable = false,length = 30)
    private TaskEventType eventType;

    @Column (name="created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected TaskEvent(){

    }

    public TaskEvent(
        long taskId,
        TaskStatus fromStatus,
        TaskStatus toStatus,
        TaskEventType eventType
    ){
        this.taskId=taskId;
        this.fromStatus=fromStatus;
        this.toStatus=toStatus;
        this.eventType=eventType;
        this.createdAt=LocalDateTime.now();
    }

    public Long getId(){
        return id;
    }

    public Long getTaskId(){
        return taskId;
    }

    public TaskStatus getFromStatus(){
        return fromStatus;
    }

    public TaskStatus getToStatus() {
        return toStatus;
    }

    public TaskEventType getEventType() {
        return eventType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }




}
