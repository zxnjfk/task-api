package com.example.task_api.worker;

import org.springframework.stereotype.Component;

import com.example.task_api.exception.AsyncJobContractException;
import com.example.task_api.model.AsyncJob;
import com.example.task_api.model.AsyncJobFailureType;
import com.example.task_api.service.AsyncJobService;
import com.example.task_api.service.TaskReminderJobHandler;

@Component
public class AsyncJobWorker {
    private final AsyncJobService asyncJobService;
    private final TaskReminderJobHandler taskReminderJobHandler;

    public AsyncJobWorker(
            AsyncJobService asyncJobService,
            TaskReminderJobHandler taskReminderJobHandler) {
        this.asyncJobService = asyncJobService;
        this.taskReminderJobHandler = taskReminderJobHandler;
    }

    public void process(long jobId) {
        AsyncJob job = asyncJobService.startJob(jobId);

        try {
            execute(job);
        } catch (AsyncJobContractException exception) {
            asyncJobService.failJob(
                    jobId,
                    AsyncJobFailureType.CONTRACT_VIOLATION,
                    buildErrorMessage(jobId, exception));
        } catch (RuntimeException exception) {
            asyncJobService.failJob(
                    jobId,
                    AsyncJobFailureType.PROCESSING_ERROR,
                    buildErrorMessage(jobId, exception));
        }

    }

    private void execute(AsyncJob job) {
        switch (job.getJobType()) {
            case TASK_REMINDER ->
                taskReminderJobHandler.handle(job.getId());

            default -> throw new AsyncJobContractException(
                    "Unsupported jobType: jobId="
                            + job.getId()
                            + ", jobType="
                            + job.getJobType());

        }
    }

    private String buildErrorMessage(
            long jobId,
            RuntimeException exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            message = "No error message available";
        }

        return "jobId="
                + jobId
                + ", exception="
                + exception.getClass().getSimpleName()
                + ", message="
                + message;
    }
}
