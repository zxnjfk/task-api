package com.example.task_api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class AsyncJobTest {

    @Test
    void shouldCompleteSuccessfulLifecycle() {
        AsyncJob job =  createJob();
        job.start();

        assertEquals(AsyncJobStatus.RUNNING, job.getStatus());
        assertNotNull(job.getStartedAt());
        assertNull(job.getFinishedAt());

        job.succeed();

        assertEquals(AsyncJobStatus.SUCCESS, job.getStatus());
        assertNotNull(job.getFinishedAt());
        assertNull(job.getErrorMessage());
    }

    @Test
    void shouldFailWithoutErrorMessage() {
        AsyncJob job =  createJob();

        job.start();
        job.fail(AsyncJobFailureType.PROCESSING_ERROR,null);

        assertEquals(AsyncJobStatus.FAILED, job.getStatus());
        assertEquals(AsyncJobFailureType.PROCESSING_ERROR, job.getFailureType());

        assertNotNull(job.getStartedAt());
        assertNotNull(job.getFinishedAt());
        assertNull(job.getErrorMessage());
        assertNull(job.getSkipReason());

    }

    @Test
    void shouldRejectFinishingPendingJob() {
        AsyncJob job =  createJob();

        assertThrows(IllegalStateException.class, job::succeed);
        assertThrows(
                IllegalStateException.class,
                () -> job.fail(AsyncJobFailureType.PROCESSING_ERROR,"Reminder delivery failed")
        );
        assertThrows(
            IllegalStateException.class,
            () -> job.skip(AsyncJobSkipReason.DEADLINE_CHANGED)
        );

        assertEquals(AsyncJobStatus.PENDING, job.getStatus());
        assertNull(job.getStartedAt());
        assertNull(job.getFinishedAt());
    }

    @Test
    void shouldSkipRunningJobWithReason(){
        AsyncJob job = createJob();

        job.start();
        job.skip(AsyncJobSkipReason.DEADLINE_CHANGED);

        assertEquals(AsyncJobStatus.SKIPPED, job.getStatus());
        assertEquals(
            AsyncJobSkipReason.DEADLINE_CHANGED,
            job.getSkipReason()
        );
        assertNotNull(job.getStartedAt());
        assertNotNull(job.getFinishedAt());
        assertNull(job.getFailureType());
        assertNull(job.getErrorMessage());
    }

    private AsyncJob createJob(){
        return new AsyncJob(
                100L,
                AsyncJobType.TASK_REMINDER,
                ReminderStage.ONE_DAY_BEFORE,
                LocalDate.of(2026,9,30)
        );
    }


}
