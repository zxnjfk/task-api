package com.example.task_api.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.task_api.model.AsyncJobStatus;
import com.example.task_api.repository.AsyncJobRepository;
import com.example.task_api.worker.AsyncJobWorker;

@Component
public class AsyncJobDispatcher {
    private static final Logger log = LoggerFactory.getLogger(AsyncJobDispatcher.class);

    private static final int BATCH_SIZE = 20;

    private final AsyncJobRepository asyncJobRepository;
    private final AsyncJobWorker asyncJobWorker;

    public AsyncJobDispatcher(
            AsyncJobRepository asyncJobRepository,
            AsyncJobWorker asyncJobWorker
    ) {
        this.asyncJobRepository = asyncJobRepository;
        this.asyncJobWorker = asyncJobWorker;
    }

    @Scheduled(fixedDelayString = "${app.async-job.dispatch-delay-ms:5000}")

    public void dispatchPendingJobs() {
        List<Long> jobIds = asyncJobRepository.findIdsByStatus(
                AsyncJobStatus.PENDING,
                PageRequest.of(0, BATCH_SIZE));

        for (Long jobId : jobIds) {
            try {
                asyncJobWorker.process(jobId);
            } catch (RuntimeException exception) {
                log.error(
                        "AsyncJob processing escaped worker: jobId={}",
                        jobId,
                        exception);
            }
        }
    }

}
