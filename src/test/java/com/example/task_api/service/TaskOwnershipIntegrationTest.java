package com.example.task_api.service;

import static com.example.task_api.support.TestUserFactory.saveUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.example.task_api.exception.TaskNotFoundException;
import com.example.task_api.model.AppUser;
import com.example.task_api.model.Priority;
import com.example.task_api.model.Task;
import com.example.task_api.repository.AppUserRepository;
import com.example.task_api.repository.TaskRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@Import(TaskService.class)
class TaskOwnershipIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnOnlyTasksOwnedByCurrentUser() {
        AppUser userA = saveUser(appUserRepository);
        AppUser userB = saveUser(appUserRepository);

        Task userATask = saveTask(userA.getId(), "User A task");
        saveTask(userB.getId(), "User B task");

        Page<Task> result = taskService.getTasks(
                userA.getId(),
                PageRequest.of(0, 10)
        );

        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(userATask.getId(), result.getContent().get(0).getId());
    }

    @Test
    void shouldRejectReadingTaskOwnedByAnotherUser() {
        AppUser userA = saveUser(appUserRepository);
        AppUser userB = saveUser(appUserRepository);

        Task userBTask = saveTask(userB.getId(), "User B task");

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.findTaskById(
                        userA.getId(),
                        userBTask.getId()
                )
        );
    }

    @Test
    void shouldRejectUpdatingTaskOwnedByAnotherUser() {
        AppUser userA = saveUser(appUserRepository);
        AppUser userB = saveUser(appUserRepository);

        Task userBTask = saveTask(userB.getId(), "Original title");
        Long originalVersion = userBTask.getVersion();

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.updateTask(
                        userA.getId(),
                        userBTask.getId(),
                        "Changed title",
                        "Changed description",
                        originalVersion
                )
        );

        entityManager.clear();

        Task reloadedTask = taskRepository.findById(userBTask.getId())
                .orElseThrow();

        assertEquals("Original title", reloadedTask.getTitle());
        assertEquals("Test description", reloadedTask.getDescription());
        assertEquals(originalVersion, reloadedTask.getVersion());
    }

    private Task saveTask(Long ownerId, String title) {
        Task task = new Task(
                ownerId,
                title,
                "Test description",
                Priority.MEDIUM,
                LocalDate.of(2026, 12, 31)
        );

        return taskRepository.saveAndFlush(task);
    }
}
