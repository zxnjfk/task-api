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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "async_jobs",
        indexes = @Index(
                name = "idx_async_jobs_status_id",
                columnList = "status, id"
        ),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_async_jobs_reminder_business_key",
                columnNames = {
                        "task_id",
                        "job_type",
                        "reminder_stage",
                        "target_deadline"
                }
        )
)
public class AsyncJob {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name="task_id",nullable=false,updatable = false)
    private Long taskId;

    @Enumerated (EnumType.STRING)
    @Column (name = "job_type",nullable = false,length = 50,updatable = false)
    private AsyncJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "reminder_stage",
            nullable = false,
            length = 30,
            updatable = false
    )
    private ReminderStage reminderStage;


    @Column(
            name = "target_deadline",
            nullable = false,
            updatable = false
    )
    private LocalDate targetDeadline;

    @Enumerated (EnumType.STRING)
    @Column (name = "status",nullable = false ,length = 20)
    private AsyncJobStatus status;

    @Enumerated (EnumType.STRING)
    @Column(name="skip_reason",length=50)
    private AsyncJobSkipReason skipReason;

    @Enumerated (EnumType.STRING)
    @Column (name="failure_type",length=50)
    private AsyncJobFailureType failureType;

    @Column(name = "error_message",columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;



    protected AsyncJob(){

    }

    public AsyncJob(
            Long taskId,
            AsyncJobType jobType,
            ReminderStage reminderStage,
            LocalDate targetDeadline
    ){
        if(taskId==null || taskId<=0){
            throw new IllegalArgumentException("taskId must be positive");
        }

        if(jobType == null){
            throw new IllegalArgumentException("jobType must not be null");
        }

        if(reminderStage == null) {
            throw new IllegalArgumentException("reminderStage must not be null");
        }

        if(targetDeadline == null) {
            throw new IllegalArgumentException("targetDeadline must not be null");
        }

        this.taskId = taskId;
        this.jobType = jobType;
        this.reminderStage = reminderStage;
        this.targetDeadline = targetDeadline;
        this.status = AsyncJobStatus.PENDING;


        LocalDateTime now = LocalDateTime.now();
        this.createdAt=now;
        this.updatedAt=now;
    }

    public void start(){
        requireStatus(AsyncJobStatus.PENDING);

        LocalDateTime now = LocalDateTime.now();
        this.status = AsyncJobStatus.RUNNING;
        this.startedAt = now;
        this.updatedAt = now;
    }

    public void succeed(){
        requireStatus(AsyncJobStatus.RUNNING);

        LocalDateTime now = LocalDateTime.now();
        this.status = AsyncJobStatus.SUCCESS;
        this.skipReason=null;
        this.failureType=null;
        this.errorMessage=null;
        this.finishedAt = now;
        this.updatedAt = now;
    }

    public void fail(AsyncJobFailureType failureType,String errorMessage) {
        requireStatus(AsyncJobStatus.RUNNING);
        if (failureType==null){
            throw new IllegalArgumentException(
                "failureType must not be null"
            );
        }

        LocalDateTime now = LocalDateTime.now();
        this.status = AsyncJobStatus.FAILED;
        this.skipReason=null;
        this.failureType=failureType;
        this.errorMessage = errorMessage;
        this.finishedAt = now;
        this.updatedAt = now;
    }

    public void skip(AsyncJobSkipReason skipReason){
        requireStatus(AsyncJobStatus.RUNNING);

        if(skipReason==null){
            throw new IllegalArgumentException(
                "skipReason must not be null"
            );
        }

        LocalDateTime now=LocalDateTime.now();
        this.status=AsyncJobStatus.SKIPPED;
        this.skipReason = skipReason;
        this.failureType=null;
        this.errorMessage=null;
        this.finishedAt=now;
        this.updatedAt=now;
    }


        public Long getId(){
        return id;
    }
    public Long getTaskId(){
        return taskId;
    }

    public AsyncJobType getJobType() {
        return jobType;
    }
    public ReminderStage getReminderStage(){
        return reminderStage;
    }

    public LocalDate getTargetDeadline(){
        return targetDeadline;
    }

    public AsyncJobStatus getStatus() {
        return status;
    }

    public AsyncJobSkipReason getSkipReason(){
        return skipReason;
    }

    public AsyncJobFailureType getFailureType(){
        return failureType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
       return finishedAt;
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

    private void requireStatus(AsyncJobStatus expectedStatus) {
        if (this.status!=expectedStatus) {
            throw new IllegalStateException(
                "AsyncJob must be " + expectedStatus + " but was " + this.status
            );
        }
    }



}
