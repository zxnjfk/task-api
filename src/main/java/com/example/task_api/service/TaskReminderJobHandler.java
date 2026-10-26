package com.example.task_api.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.task_api.exception.AsyncJobContractException;
import com.example.task_api.model.AsyncJob;
import com.example.task_api.model.AsyncJobStatus;

import com.example.task_api.model.ReminderStage;
import com.example.task_api.model.Task;
import com.example.task_api.model.TaskStatus;
import com.example.task_api.model.AsyncJobSkipReason;
import com.example.task_api.repository.AsyncJobRepository;
import com.example.task_api.repository.TaskRepository;

@Service
public class TaskReminderJobHandler {

    private final AsyncJobRepository asyncJobRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    public TaskReminderJobHandler(
            AsyncJobRepository asyncJobRepository,
            TaskRepository taskRepository,
            NotificationService notificationService,
            Clock clock) {
        this.asyncJobRepository = asyncJobRepository;
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Transactional
    public void handle(long jobId) {
        AsyncJob job = asyncJobRepository.findById(jobId)
                .orElseThrow(() -> new AsyncJobContractException(
                        "AsyncJob not found: jobId=" + jobId));

        validateJobContract(job);

        Optional<Task> taskResult = taskRepository.findById(job.getTaskId());

        if (taskResult.isEmpty()) {
            job.skip(AsyncJobSkipReason.TASK_NOT_FOUND);
            return;
        }

        Task task = taskResult.get();

        if (!isRemindable(task)) {
            job.skip(AsyncJobSkipReason.TASK_NOT_REMINDABLE);
            return;
        }

        if (!task.getDeadline().equals(job.getTargetDeadline())) {
            job.skip(AsyncJobSkipReason.DEADLINE_CHANGED);
            return;
        }
        LocalDate reminderDate = job.getTargetDeadline().minusDays(1);
        if (!LocalDate.now(clock).equals(reminderDate)) {
            job.skip(AsyncJobSkipReason.REMINDER_WINDOW_MISSED);
            return;
        }

        notificationService.getOrCreateTaskReminderNotification(job, task);
        job.succeed();

    }

    private void validateJobContract(AsyncJob job) {
        if (job.getStatus() != AsyncJobStatus.RUNNING) {
            throw new AsyncJobContractException(
                    "AsyncJob must be RUNNING: jobId="
                            + job.getId()
                            + ", status="
                            + job.getStatus());
        }

        if (job.getReminderStage() != ReminderStage.ONE_DAY_BEFORE) {
            throw new AsyncJobContractException(
                    "Unsupported reminderStage: jobId="
                            + job.getId()
                            + ", reminderStage="
                            + job.getReminderStage());
        }

    }

    private boolean isRemindable(Task task) {
        return task.getStatus() == TaskStatus.TODO
                || task.getStatus() == TaskStatus.IN_PROGRESS;
    }

}
