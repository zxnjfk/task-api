package com.example.task_api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.example.task_api.model.AsyncJob;
import com.example.task_api.model.AsyncJobStatus;
import com.example.task_api.model.AsyncJobType;
import com.example.task_api.model.ReminderStage;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
class AsyncJobRepositoryTest {

    @Autowired
    private AsyncJobRepository asyncJobRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveNewAsyncJobAsPending() {
        LocalDate targetDeadline = LocalDate.of(2026, 9, 30);

        AsyncJob job = new AsyncJob(
                100L,
                AsyncJobType.TASK_REMINDER,
                ReminderStage.ONE_DAY_BEFORE,
                targetDeadline
        );

        AsyncJob savedJob = asyncJobRepository.saveAndFlush(job);
        Long savedJobId = savedJob.getId();

        assertNotNull(savedJobId);

        entityManager.clear();

        AsyncJob reloadedJob = asyncJobRepository.findById(savedJobId)
                .orElseThrow();

        assertEquals(100L, reloadedJob.getTaskId());
        assertEquals(AsyncJobType.TASK_REMINDER, reloadedJob.getJobType());
        assertEquals(ReminderStage.ONE_DAY_BEFORE, reloadedJob.getReminderStage());
        assertEquals(targetDeadline, reloadedJob.getTargetDeadline());
        assertEquals(AsyncJobStatus.PENDING, reloadedJob.getStatus());

        assertNull(reloadedJob.getStartedAt());
        assertNull(reloadedJob.getFinishedAt());
        assertNull(reloadedJob.getSkipReason());
        assertNull(reloadedJob.getFailureType());
        assertNull(reloadedJob.getErrorMessage());

        assertNotNull(reloadedJob.getCreatedAt());
        assertNotNull(reloadedJob.getUpdatedAt());
        assertNotNull(reloadedJob.getVersion());
    }

    @Test
    void shouldRejectDuplicateReminderBusinessKey() {
        LocalDate targetDeadline = LocalDate.of(2026, 9, 30);

        AsyncJob firstJob = new AsyncJob(
                100L,
                AsyncJobType.TASK_REMINDER,
                ReminderStage.ONE_DAY_BEFORE,
                targetDeadline
        );

        AsyncJob duplicateJob = new AsyncJob(
                100L,
                AsyncJobType.TASK_REMINDER,
                ReminderStage.ONE_DAY_BEFORE,
                targetDeadline
        );

        asyncJobRepository.saveAndFlush(firstJob);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> asyncJobRepository.saveAndFlush(duplicateJob)
        );
    }
}