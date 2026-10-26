package com.example.task_api.scheduler;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.task_api.model.Task;
import com.example.task_api.model.TaskStatus;
import com.example.task_api.repository.TaskRepository;
import com.example.task_api.service.AsyncJobService;

@Component
public class TaskReminderScheduler {
    private final TaskRepository taskRepository;
    private final AsyncJobService asyncJobService;
    private final Clock clock;

    public TaskReminderScheduler(
            TaskRepository taskRepository,
            AsyncJobService asyncJobService,
            Clock clock) {
        this.taskRepository = taskRepository;
        this.asyncJobService = asyncJobService;
        this.clock = clock;
    }

    @Scheduled(cron = "${app.reminder.cron:0 0 9 * * *}", zone = "Asia/Shanghai")
    public void createReminderJobs() {
        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);

        List<Task> candidates = taskRepository.findByDeadlineAndStatusIn(
                tomorrow,
                List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS));

        for (Task task : candidates) {
            asyncJobService.createTaskReminderJobIfEligible(task.getId());
        }

    }

}
