package com.example.task_api.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import com.example.task_api.service.AsyncJobService;
import com.example.task_api.model.AppUser;
import com.example.task_api.repository.AppUserRepository;
import static com.example.task_api.support.TestUserFactory.saveUser;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        TaskReminderScheduler.class,
        AsyncJobService.class
})
class TaskReminderSchedulerTest {

    @Autowired
    private TaskReminderScheduler taskReminderScheduler;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AsyncJobRepository asyncJobRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUpClock() {
        when(clock.getZone()).thenReturn(ZoneId.of("Asia/Shanghai"));
        when(clock.instant())
                .thenReturn(Instant.parse("2026-09-29T01:00:00Z"));
    }

    @Test
    void shouldCreateReminderJobForTaskDueTomorrow() {
        AppUser appUser = saveUser(appUserRepository);

        Task task = new Task(
                appUser.getId(),
                "Prepare report",
                "Complete before deadline",
                Priority.MEDIUM,
                LocalDate.of(2026, 9, 30)
        );
        Task savedTask = taskRepository.saveAndFlush(task);

        taskReminderScheduler.createReminderJobs();


        List<AsyncJob> jobs = asyncJobRepository.findAll();

        assertEquals(1, jobs.size());

        AsyncJob job = jobs.get(0);

        assertEquals(savedTask.getId(), job.getTaskId());
        assertEquals(LocalDate.of(2026, 9, 30), job.getTargetDeadline());

    }
}
