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

import com.example.task_api.exception.NotificationNotFoundException;
import com.example.task_api.model.AppUser;
import com.example.task_api.model.AsyncJob;
import com.example.task_api.model.AsyncJobType;
import com.example.task_api.model.Notification;
import com.example.task_api.model.NotificationStatus;
import com.example.task_api.model.NotificationType;
import com.example.task_api.model.Priority;
import com.example.task_api.model.ReminderStage;
import com.example.task_api.model.Task;
import com.example.task_api.repository.AppUserRepository;
import com.example.task_api.repository.AsyncJobRepository;
import com.example.task_api.repository.NotificationRepository;
import com.example.task_api.repository.TaskRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@Import(NotificationService.class)
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AsyncJobRepository asyncJobRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnOnlyNotificationsBelongingToRecipient() {
        AppUser firstUser = saveUser(appUserRepository);
        AppUser secondUser = saveUser(appUserRepository);

        Notification firstUserNotification = createNotification(
                firstUser,
                "First user's task"
        );
        createNotification(
                secondUser,
                "Second user's task"
        );

        Page<Notification> result = notificationService.getNotifications(
                firstUser.getId(),
                PageRequest.of(0, 10)
        );

        assertEquals(1L, result.getTotalElements());
        assertEquals(
                firstUserNotification.getId(),
                result.getContent().get(0).getId()
        );
        assertEquals(
                firstUser.getId(),
                result.getContent().get(0).getRecipientId()
        );
    }

    @Test
    void shouldMarkOwnNotificationAsReadIdempotentlyAndRejectAnotherRecipient() {
        AppUser firstUser = saveUser(appUserRepository);
        AppUser secondUser = saveUser(appUserRepository);

        Notification ownNotification = createNotification(
                firstUser,
                "First user's task"
        );
        Notification anotherUsersNotification = createNotification(
                secondUser,
                "Second user's task"
        );

        Notification firstResult = notificationService.markAsRead(
                firstUser.getId(),
                ownNotification.getId()
        );
        notificationRepository.flush();

        Long versionAfterFirstRead = firstResult.getVersion();

        entityManager.clear();

        Notification secondResult = notificationService.markAsRead(
                firstUser.getId(),
                ownNotification.getId()
        );
        notificationRepository.flush();

        assertEquals(NotificationStatus.READ, secondResult.getStatus());
        assertEquals(versionAfterFirstRead, secondResult.getVersion());

        assertThrows(
                NotificationNotFoundException.class,
                () -> notificationService.markAsRead(
                        firstUser.getId(),
                        anotherUsersNotification.getId()
                )
        );
        assertEquals(
                NotificationStatus.UNREAD,
                anotherUsersNotification.getStatus()
        );
    }

    private Notification createNotification(
            AppUser user,
            String taskTitle
    ) {
        LocalDate targetDeadline = LocalDate.of(2026, 9, 30);

        Task task = new Task(
                user.getId(),
                taskTitle,
                "Complete before deadline",
                Priority.MEDIUM,
                targetDeadline
        );
        Task savedTask = taskRepository.saveAndFlush(task);

        AsyncJob job = new AsyncJob(
                savedTask.getId(),
                AsyncJobType.TASK_REMINDER,
                ReminderStage.ONE_DAY_BEFORE,
                targetDeadline
        );
        AsyncJob savedJob = asyncJobRepository.saveAndFlush(job);

        Notification notification = new Notification(
                user.getId(),
                savedTask.getId(),
                savedJob.getId(),
                NotificationType.TASK_REMINDER,
                "Task \"" + taskTitle + "\" is due tomorrow"
        );

        return notificationRepository.saveAndFlush(notification);
    }
}
