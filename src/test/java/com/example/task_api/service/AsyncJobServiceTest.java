package com.example.task_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static com.example.task_api.support.TestUserFactory.saveUser;

import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.task_api.model.AsyncJob;
import com.example.task_api.model.Priority;
import com.example.task_api.model.Task;
import com.example.task_api.repository.AsyncJobRepository;
import com.example.task_api.repository.TaskRepository;
import com.example.task_api.model.AppUser;
import com.example.task_api.repository.AppUserRepository;

@DataJpaTest
@ActiveProfiles("test")
@Import(AsyncJobService.class)
class AsyncJobServiceTest {

    @Autowired
    private AsyncJobService asyncJobService;

    @Autowired
    private AsyncJobRepository asyncJobRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    private Long ownerId;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUpclock() {
        when(clock.getZone()).thenReturn(ZoneId.of("Asia/Shanghai"));
        when(clock.instant()).thenReturn(Instant.parse("2026-09-29T01:00:00Z"));

        AppUser user = saveUser(appUserRepository);
        ownerId = user.getId();
    }

    @Test
    void shouldReturnExistingReminderJobWhenBusinessKeyAlreadyExists() {
        Task task = new Task(
                ownerId,
                "Prepare report",
                "Complete before deadline",
                Priority.MEDIUM,
                LocalDate.of(2026, 9, 30));

        Task savedTask = taskRepository.saveAndFlush(task);

        AsyncJob firstJob = asyncJobService
                .createTaskReminderJobIfEligible(savedTask.getId())
                .orElseThrow();

        AsyncJob secondJob = asyncJobService
                .createTaskReminderJobIfEligible(savedTask.getId())
                .orElseThrow();

        assertEquals(firstJob.getId(), secondJob.getId());

        assertEquals(1L, asyncJobRepository.count());

    }

    @Test
    void shouldCreateNewReminderJobWhenChangedDeadlineBecomesTomorrow() {
        LocalDate originalDeadline = LocalDate.of(2026, 9, 30);
        LocalDate newDeadline = LocalDate.of(2026, 10, 5);

        Task task = new Task(
                ownerId,
                "Prepare report",
                "Complete before deadline",
                Priority.MEDIUM,
                originalDeadline);

        Task savedTask = taskRepository.saveAndFlush(task);

        AsyncJob firstJob = asyncJobService
                .createTaskReminderJobIfEligible(savedTask.getId())
                .orElseThrow();

        savedTask.setDeadline(newDeadline);
        taskRepository.flush();

        when(clock.instant())
                .thenReturn(Instant.parse("2026-10-04T01:00:00Z"));

        AsyncJob secondJob = asyncJobService
                .createTaskReminderJobIfEligible(savedTask.getId())
                .orElseThrow();// 如果这个明明符合条件的 Task 没有生成 Job，测试立即失败。

        assertNotEquals(firstJob.getId(), secondJob.getId());
        assertEquals(originalDeadline, firstJob.getTargetDeadline());
        assertEquals(newDeadline, secondJob.getTargetDeadline());
        assertEquals(2L, asyncJobRepository.count());

    }

    @Test
    void shouldNotCreateReminderJobForCompletedTask() {
        Task task = new Task(
                ownerId,
                "Prepare report",
                "Complete before deadline",
                Priority.MEDIUM,
                LocalDate.of(2026, 9, 30));

        Task savedTask = taskRepository.saveAndFlush(task);

        savedTask.start();
        savedTask.complete();
        taskRepository.flush();

        Optional<AsyncJob> result = asyncJobService
                .createTaskReminderJobIfEligible(savedTask.getId());
        assertTrue(result.isEmpty());
        assertEquals(0L, asyncJobRepository.count());
    }

    @Test
    void shouldNotCreateReminderJobWhenDeadlineIsNotTomorrow() {
        Task task = new Task(
                ownerId,
                "Prepare report",
                "Complete before deadline",
                Priority.MEDIUM,
                LocalDate.of(2026, 10, 1));
        Task savedTask = taskRepository.saveAndFlush(task);

        Optional<AsyncJob> result = asyncJobService
                .createTaskReminderJobIfEligible(savedTask.getId());
        assertTrue(result.isEmpty());
        assertEquals(0L, asyncJobRepository.count());

    }

}
