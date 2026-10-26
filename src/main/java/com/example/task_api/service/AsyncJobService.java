package com.example.task_api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.task_api.exception.TaskNotFoundException;
import com.example.task_api.model.AsyncJob;
import com.example.task_api.model.AsyncJobType;
import com.example.task_api.model.ReminderStage;
import com.example.task_api.model.Task;
import com.example.task_api.repository.AsyncJobRepository;
import com.example.task_api.repository.TaskRepository;
import com.example.task_api.exception.AsyncJobNotFoundException;
import com.example.task_api.model.TaskStatus;
import com.example.task_api.model.AsyncJobFailureType;

import java.time.LocalDate;
import java.util.Optional;
import java.time.Clock;

@Service
public class AsyncJobService {

    private final AsyncJobRepository asyncJobRepository;
    private final TaskRepository taskRepository;
    private final Clock clock;

    public AsyncJobService(
            AsyncJobRepository asyncJobRepository,
            TaskRepository taskRepository,
            Clock clock
    ) {
        this.asyncJobRepository = asyncJobRepository;
        this.taskRepository = taskRepository;
        this.clock=clock;
    }

    @Transactional
    public Optional<AsyncJob> createTaskReminderJobIfEligible(long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if(!isEligibleForOneDayBeforeReminder(task)){
            return Optional.empty();
        }

        AsyncJobType jobType = AsyncJobType.TASK_REMINDER;
        ReminderStage reminderStage = ReminderStage.ONE_DAY_BEFORE;
        LocalDate targetDeadline = task.getDeadline();



        Optional<AsyncJob> existingJob =
                asyncJobRepository
                        .findByTaskIdAndJobTypeAndReminderStageAndTargetDeadline(
                                task.getId(),
                                jobType,
                                reminderStage,
                                targetDeadline
                        );
        if(existingJob.isPresent()){
            return existingJob;
        }

        AsyncJob job = new AsyncJob(
                task.getId(),
                jobType,
                reminderStage,
                targetDeadline
        );

        return Optional.of(asyncJobRepository.save(job));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AsyncJob startJob(long jobId){
        AsyncJob job = findJobById(jobId);
        job.start();
        return job;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AsyncJob failJob(long jobId, AsyncJobFailureType failureType,String errorMessage) {
        AsyncJob job = findJobById(jobId);

        job.fail(failureType,errorMessage);

        return job;
    }

    private boolean isEligibleForOneDayBeforeReminder(Task task){
        boolean unfinished =
                task.getStatus() == TaskStatus.TODO
                        || task.getStatus() == TaskStatus.IN_PROGRESS;

        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);

        return unfinished && task.getDeadline().equals(tomorrow);
    }

    private AsyncJob findJobById(long jobId) {
        return  asyncJobRepository.findById(jobId)
                 .orElseThrow(() -> new AsyncJobNotFoundException(jobId));
    }





}
