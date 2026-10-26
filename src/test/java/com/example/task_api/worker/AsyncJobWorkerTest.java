package com.example.task_api.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static com.example.task_api.support.TestUserFactory.saveUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.util.AopTestUtils;

import com.example.task_api.model.AsyncJob;
import com.example.task_api.model.AsyncJobStatus;
import com.example.task_api.model.AsyncJobType;
import com.example.task_api.model.Notification;
import com.example.task_api.model.NotificationStatus;
import com.example.task_api.model.Priority;
import com.example.task_api.model.ReminderStage;
import com.example.task_api.model.Task;
import com.example.task_api.repository.AsyncJobRepository;
import com.example.task_api.repository.NotificationRepository;
import com.example.task_api.repository.TaskRepository;
import com.example.task_api.service.AsyncJobService;
import com.example.task_api.service.NotificationService;
import com.example.task_api.service.TaskReminderJobHandler;
import com.example.task_api.model.AsyncJobSkipReason;
import com.example.task_api.model.AsyncJobFailureType;
import com.example.task_api.model.AppUser;
import com.example.task_api.repository.AppUserRepository;

@DataJpaTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({
                AsyncJobWorker.class,
                AsyncJobService.class,
                TaskReminderJobHandler.class,
                NotificationService.class
}) // 构造器需要所以要import，但测试类不直接用不用autowired
class AsyncJobWorkerTest {

        @Autowired
        private AsyncJobWorker asyncJobWorker;

        @Autowired
        private TaskRepository taskRepository;

        @Autowired
        private AsyncJobRepository asyncJobRepository;

        @Autowired
        private NotificationRepository notificationRepository;

        @Autowired
        private AppUserRepository appUserRepository;

        private Long ownerId;

        @MockitoBean
        private Clock clock;

        @MockitoSpyBean
        private NotificationService notificationService;

        @BeforeEach
        void setUpClock() {
                when(clock.getZone())
                                .thenReturn(ZoneId.of("Asia/Shanghai"));

                when(clock.instant())
                                .thenReturn(Instant.parse("2026-09-29T01:00:00Z"));

                AppUser user = saveUser(appUserRepository);
                ownerId = user.getId();

        }

        @Test
        void shouldCreateNotificationAndSucceedJob() {
                Task task = new Task(
                                ownerId,
                                "Prepare report",
                                "Complete before deadline",
                                Priority.MEDIUM,
                                LocalDate.of(2026, 9, 30));
                Task savedTask = taskRepository.saveAndFlush(task);

                AsyncJob job = new AsyncJob(
                                savedTask.getId(),
                                AsyncJobType.TASK_REMINDER,
                                ReminderStage.ONE_DAY_BEFORE,
                                savedTask.getDeadline());

                AsyncJob savedJob = asyncJobRepository.saveAndFlush(job);

                asyncJobWorker.process(savedJob.getId());

                AsyncJob reloadedJob = asyncJobRepository.findById(savedJob.getId())
                                .orElseThrow();

                Notification notification = notificationRepository
                                .findByAsyncJobId(savedJob.getId())
                                .orElseThrow();

                assertEquals(
                                AsyncJobStatus.SUCCESS,
                                reloadedJob.getStatus());

                assertNotNull(reloadedJob.getStartedAt());
                assertNotNull(reloadedJob.getFinishedAt());
                assertNull(reloadedJob.getSkipReason());
                assertNull(reloadedJob.getFailureType());
                assertNull(reloadedJob.getErrorMessage());

                assertEquals(savedTask.getId(), notification.getTaskId());
                assertEquals(savedJob.getId(), notification.getAsyncJobId());
                assertEquals(
                                NotificationStatus.UNREAD,
                                notification.getStatus());
                assertEquals(
                                "Task \"Prepare report\" is due on 2026-09-30",
                                notification.getContent());

                assertEquals(ownerId, notification.getRecipientId());

        }

        @Test
        void shouldSkipJobWhenTaskDeadlineHasChanged() {
                LocalDate originalDeadline = LocalDate.of(2026, 9, 30);
                LocalDate changedDeadline = LocalDate.of(2026, 10, 1);

                Task task = new Task(
                                ownerId,
                                "Prepare report",
                                "Complete before deadline",
                                Priority.MEDIUM,
                                originalDeadline);

                Task savedTask = taskRepository.saveAndFlush(task);

                AsyncJob job = new AsyncJob(
                                savedTask.getId(),
                                AsyncJobType.TASK_REMINDER,
                                ReminderStage.ONE_DAY_BEFORE,
                                originalDeadline);

                AsyncJob savedJob = asyncJobRepository.saveAndFlush(job);
                Long jobId = savedJob.getId();

                savedTask.setDeadline(changedDeadline);
                taskRepository.saveAndFlush(savedTask);

                asyncJobWorker.process(jobId);// 方法返回后，savedJob 不再由那个已经结束的持久化上下文持续管理，可以把它理解为 detached 对象。

                AsyncJob reloadedJob = asyncJobRepository.findById(jobId)
                                .orElseThrow();

                assertEquals(
                                AsyncJobStatus.SKIPPED,
                                reloadedJob.getStatus());

                assertEquals(
                                AsyncJobSkipReason.DEADLINE_CHANGED,
                                reloadedJob.getSkipReason());

                assertNotNull(reloadedJob.getStartedAt());
                assertNotNull(reloadedJob.getFinishedAt());
                assertNull(reloadedJob.getFailureType());
                assertNull(reloadedJob.getErrorMessage());

                assertTrue(notificationRepository
                                .findByAsyncJobId(jobId)
                                .isEmpty());

        }

        @Test
        void shouldFailJobAndRollbackNotificationWhenCreationFails() {
                Task task = new Task(
                                ownerId,
                                "Prepare report",
                                "Complete before deadline",
                                Priority.MEDIUM,
                                LocalDate.of(2026, 9, 30));

                Task savedTask = taskRepository.saveAndFlush(task);

                AsyncJob job = new AsyncJob(
                                savedTask.getId(),
                                AsyncJobType.TASK_REMINDER,
                                ReminderStage.ONE_DAY_BEFORE,
                                savedTask.getDeadline());

                AsyncJob savedJob = asyncJobRepository.saveAndFlush(job);

                Long jobId = savedJob.getId();

                NotificationService notificationServiceTarget = AopTestUtils
                                .getUltimateTargetObject(notificationService);

                doAnswer(invocation -> {
                        invocation.callRealMethod();

                        throw new IllegalStateException(
                                        "Notification creation failed");
                }).when(notificationServiceTarget)
                                .getOrCreateTaskReminderNotification(
                                                any(AsyncJob.class),
                                                any(Task.class));// 不在乎具体是哪个实例

                asyncJobWorker.process(jobId);

                AsyncJob reloadedJob = asyncJobRepository.findById(jobId)
                                .orElseThrow();

                assertEquals(
                                AsyncJobStatus.FAILED,
                                reloadedJob.getStatus());

                assertEquals(
                                AsyncJobFailureType.PROCESSING_ERROR,
                                reloadedJob.getFailureType());

                assertNotNull(reloadedJob.getStartedAt());
                assertNotNull(reloadedJob.getFinishedAt());
                assertNull(reloadedJob.getSkipReason());

                assertEquals(
                                "jobId="
                                                + jobId
                                                + ", exception=IllegalStateException"
                                                + ", message=Notification creation failed",
                                reloadedJob.getErrorMessage());

                assertTrue(
                                notificationRepository
                                                .findByAsyncJobId(jobId)
                                                .isEmpty());

        }

        @Test
        void shouldSkipJobWhenReferencedTaskNoLongerExists() {
                Task task = new Task(
                                ownerId,
                                "Prepare report",
                                "Complete before deadline",
                                Priority.MEDIUM,
                                LocalDate.of(2026, 9, 30));
                Task savedTask = taskRepository.saveAndFlush(task);

                AsyncJob job = new AsyncJob(
                                savedTask.getId(),
                                AsyncJobType.TASK_REMINDER,
                                ReminderStage.ONE_DAY_BEFORE,
                                savedTask.getDeadline());
                AsyncJob savedJob = asyncJobRepository.saveAndFlush(job);

                Long jobId = savedJob.getId();

                taskRepository.deleteById(savedTask.getId());

                asyncJobWorker.process(jobId);

                AsyncJob reloadedJob = asyncJobRepository.findById(jobId)
                                .orElseThrow();

                assertEquals(
                                AsyncJobStatus.SKIPPED,
                                reloadedJob.getStatus());
                assertEquals(
                                AsyncJobSkipReason.TASK_NOT_FOUND,
                                reloadedJob.getSkipReason());

                assertNotNull(reloadedJob.getStartedAt());
                assertNotNull(reloadedJob.getFinishedAt());
                assertNull(reloadedJob.getFailureType());
                assertNull(reloadedJob.getErrorMessage());

                assertTrue(
                                notificationRepository
                                                .findByAsyncJobId(jobId)
                                                .isEmpty());
        }

}
