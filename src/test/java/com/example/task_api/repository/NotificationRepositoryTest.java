package com.example.task_api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.example.task_api.support.TestUserFactory.saveUser;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.example.task_api.model.AsyncJob;
import com.example.task_api.model.AsyncJobType;
import com.example.task_api.model.Notification;
import com.example.task_api.model.NotificationStatus;
import com.example.task_api.model.NotificationType;
import com.example.task_api.model.ReminderStage;
import com.example.task_api.model.AppUser;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

        @Autowired
        private NotificationRepository notificationRepository;

        @Autowired
        private AsyncJobRepository asyncJobRepository;

        @Autowired
        private EntityManager entityManager;

        @Autowired
        private AppUserRepository appUserRepository;

        @Test
        void shouldSaveAndFindNotificationByAsyncJobId() {
                AppUser user = saveUser(appUserRepository);

                AsyncJob job = new AsyncJob(
                                100L,
                                AsyncJobType.TASK_REMINDER,
                                ReminderStage.ONE_DAY_BEFORE,
                                LocalDate.of(2026, 9, 30));
                AsyncJob savedJob = asyncJobRepository.saveAndFlush(job);

                Notification notification = new Notification(
                                user.getId(),
                                savedJob.getTaskId(),
                                savedJob.getId(),
                                NotificationType.TASK_REMINDER,
                                "Task 100 is due tomorrow");
                Notification savedNotification = notificationRepository.saveAndFlush(notification);

                assertNotNull(savedNotification.getId());

                entityManager.clear();

                Notification reloadedNotification = notificationRepository
                                .findByAsyncJobId(savedJob.getId())
                                .orElseThrow();

                assertEquals(
                                savedJob.getTaskId(),
                                reloadedNotification.getTaskId());
                assertEquals(
                                savedJob.getId(),
                                reloadedNotification.getAsyncJobId());
                assertEquals(
                                NotificationType.TASK_REMINDER,
                                reloadedNotification.getNotificationType());
                assertEquals(
                                NotificationStatus.UNREAD,
                                reloadedNotification.getStatus());
                assertEquals(
                                "Task 100 is due tomorrow",
                                reloadedNotification.getContent());

                assertEquals(user.getId(), reloadedNotification.getRecipientId());

                assertNotNull(reloadedNotification.getCreatedAt());
                assertNotNull(reloadedNotification.getUpdatedAt());
                assertNotNull(reloadedNotification.getVersion());
        }

        @Test
        void shouldRejectDuplicateNotificationForSameAsyncJob() {
                AppUser user = saveUser(appUserRepository);

                AsyncJob job = new AsyncJob(
                                100L,
                                AsyncJobType.TASK_REMINDER,
                                ReminderStage.ONE_DAY_BEFORE,
                                LocalDate.of(2026, 9, 30));
                AsyncJob savedJob = asyncJobRepository.saveAndFlush(job);

                Notification firstNotification = new Notification(
                                user.getId(),
                                savedJob.getTaskId(),
                                savedJob.getId(),
                                NotificationType.TASK_REMINDER,
                                "Task 100 is due tomorrow");

                Notification duplicateNotification = new Notification(
                                user.getId(),
                                savedJob.getTaskId(),
                                savedJob.getId(),
                                NotificationType.TASK_REMINDER,
                                "Duplicate reminder");//这个测试触发重复的根本原因仍然是UNIQUE(async_job_id)

                notificationRepository.saveAndFlush(firstNotification);

                assertThrows(
                                DataIntegrityViolationException.class,
                                () -> notificationRepository.saveAndFlush(
                                                duplicateNotification));
        }

}