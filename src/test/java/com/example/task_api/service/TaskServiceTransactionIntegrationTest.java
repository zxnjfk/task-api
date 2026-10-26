//只有使用真实 Repository 和 H2，才会真实发生：从数据库 SELECT；
//Task 进入持久化上下文；
//JPA 脏检查；
//Spring事务回滚；
//再次从数据库 SELECT。

package com.example.task_api.service;

import com.example.task_api.repository.TaskEventRepository;
import com.example.task_api.repository.TaskRepository;
import com.example.task_api.model.Priority;
import com.example.task_api.model.Task;
import com.example.task_api.model.TaskEvent;
import com.example.task_api.model.TaskStatus;
import com.example.task_api.model.TaskEventType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.task_api.model.AppUser;
import com.example.task_api.repository.AppUserRepository;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static com.example.task_api.support.TestUserFactory.saveUser;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceTransactionIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @MockitoSpyBean
    private TaskEventRepository taskEventRepository;

    private Long taskId;
    private Long ownerId;

    @BeforeEach // 表示这个方法在每个@Test方法执行前运行一次
    void setUp() {
        taskEventRepository.deleteAll();
        taskRepository.deleteAll();

        AppUser user = saveUser(appUserRepository);
        ownerId = user.getId();

        Task task = new Task(
                ownerId,
                "事务回滚测试",
                "验证 TaskEvent 保存失败时 Task 状态回滚",
                Priority.HIGH,
                LocalDate.of(2026, 12, 31));

        Task savedTask = taskRepository.saveAndFlush(task);// 这个调用发生在startTask()事务之前,目的是确保数据库已经真实存在一条status=TODO
        taskId = savedTask.getId();
    }

    @Test
    void shouldRollbackTaskWhenTaskEventSaveFails() {
        doThrow(
                new DataIntegrityViolationException(
                        "故意模拟 TaskEvent 保存失败"))
                .when(taskEventRepository)
                .save(any(TaskEvent.class));// 当测试中调用 taskEventRepository.save()，不要执行真实保存，直接抛出指定异常

        assertThrows(DataIntegrityViolationException.class,
                () -> taskService.startTask(ownerId, taskId));

        Task reloadedTask = taskRepository
                .findById(taskId)
                .orElseThrow();

        assertEquals(TaskStatus.TODO, reloadedTask.getStatus());

        assertEquals(
                0,
                taskEventRepository
                        .findByTaskIdOrderByIdAsc(taskId)
                        .size());

    }

    @Test
    void shouldStartTaskAndSaveStartedEvent() {
        taskService.startTask(ownerId, taskId);

        Task reloadedTask = taskRepository
                .findById(taskId)
                .orElseThrow();

        List<TaskEvent> history = taskEventRepository.findByTaskIdOrderByIdAsc(taskId);

        assertEquals(TaskStatus.IN_PROGRESS, reloadedTask.getStatus());

        assertEquals(1, history.size());

        TaskEvent event = history.get(0);

        assertEquals(TaskStatus.TODO, event.getFromStatus());
        assertEquals(TaskStatus.IN_PROGRESS, event.getToStatus());
        assertEquals(TaskEventType.STARTED, event.getEventType());
    }

    @Test
    void shouldCompleteTaskAndSaveCompletedEvent() {
        taskService.startTask(ownerId, taskId);

        taskService.completeTask(ownerId, taskId);

        Task reloadedTask = taskRepository
                .findById(taskId)
                .orElseThrow();

        List<TaskEvent> history = taskEventRepository.findByTaskIdOrderByIdAsc(taskId);

        assertEquals(
                TaskStatus.COMPLETED,
                reloadedTask.getStatus());

        assertEquals(2, history.size());

        TaskEvent completedEvent = history.get(1);

        assertEquals(
                TaskStatus.IN_PROGRESS,
                completedEvent.getFromStatus());

        assertEquals(
                TaskStatus.COMPLETED,
                completedEvent.getToStatus());

        assertEquals(
                TaskEventType.COMPLETED,
                completedEvent.getEventType());

    }

}
