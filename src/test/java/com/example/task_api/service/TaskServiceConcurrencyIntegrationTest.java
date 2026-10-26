package com.example.task_api.service;

import com.example.task_api.model.Priority;
import com.example.task_api.model.Task;
import com.example.task_api.repository.TaskEventRepository;
import com.example.task_api.repository.TaskRepository;
import com.example.task_api.model.TaskEvent;
import com.example.task_api.model.TaskEventType;
import com.example.task_api.model.TaskStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static com.example.task_api.support.TestUserFactory.saveUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import com.example.task_api.model.AppUser;
import com.example.task_api.repository.AppUserRepository;

import static org.mockito.Mockito.doAnswer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceConcurrencyIntegrationTest {
    @Autowired
    private TaskService taskService;

    @MockitoSpyBean
    private TaskRepository taskRepository;

    @Autowired
    private TaskEventRepository taskEventRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Long taskId;
    private Long ownerId;
    private Long initialVersion;

    @BeforeEach
    void setUp() {
        taskEventRepository.deleteAll();
        taskRepository.deleteAll();// 从业务关系上，TaskEvent 依赖 Task，所以先清理事件、再清理任务更合理

        AppUser user = saveUser(appUserRepository);
        ownerId = user.getId();

        Task task = new Task(
                ownerId,
                "并发启动测试",
                "两个线程同时启动同一个任务",
                Priority.HIGH,
                LocalDate.of(2026, 12, 31));

        Task savedTask = taskRepository.saveAndFlush(task);// save：让新实体进入持久化流程 flush：立即把待执行的 INSERT SQL 发送到数据库
                                                           // ,执行完sql后仍可以回滚

        taskId = savedTask.getId();
        initialVersion = savedTask.getVersion();

    }

    @Test
    void shouldAllowOnlyOneConcurrentStart() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch bothLoadedLatch = new CountDownLatch(2);// 两个事务都已经真实读取 Task

        doAnswer(invocation -> {
            Long requestedTaskId = invocation.getArgument(1);// 第二个参数是 taskId；第一个参数是 ownerId
                                                             // 对象，代表本次被拦截的方法调用.取出这个方法的第零号参数

            Task loadedTask = entityManager.find(
                    Task.class,
                    requestedTaskId);

            Optional<Task> result = Optional.ofNullable(loadedTask);

            bothLoadedLatch.countDown();// 报告当前线程已完成真实查询

            boolean bothLoaded = bothLoadedLatch.await(5, TimeUnit.SECONDS);//// 计数未归零时在这里阻塞

            if (!bothLoaded) {
                throw new IllegalStateException(
                        "两个事务未能在 5 秒内完成 Task 查询");
            }
            return result;

        }).when(taskRepository)
                .findByOwnerIdAndId(ownerId, taskId);

        try {
            Callable<Task> startAction = () -> {
                readyLatch.countDown();// 当前工作线程已经到达起跑线

                startLatch.await();

                return taskService.startTask(ownerId, taskId);

            };

            Future<Task> firstFuture = executor.submit(startAction);// executor是线程池对象,主线程把任务丢尽线程池的任务队列,线程池内部的空闲工作线程会取出这个startAction
            Future<Task> secondFuture = executor.submit(startAction);

            boolean bothReady = readyLatch.await(5, TimeUnit.SECONDS);
            assertTrue(bothReady);// 断言失败会抛出 AssertionError,测试无法继续表示失败

            startLatch.countDown();

            List<Future<Task>> futures = List.of(firstFuture, secondFuture);// List.of()用来快速创建一个不可变的List

            int successCount = 0;

            List<Throwable> failures = new ArrayList<>();// throwable是所有exception和error的顶层父类

            for (Future<Task> future : futures) {
                try {
                    future.get(10, TimeUnit.SECONDS);
                    successCount++;
                } catch (ExecutionException exception) {
                    failures.add(exception.getCause());
                } // 如果10秒内没跑完,会抛出TimeoutException,这个异常不在当前catch块,整个测试方法会抛出标记失败,不会加入到failures

            }

            assertEquals(1, failures.size());
            assertEquals(1, successCount);

            Task reloadedTask = taskRepository
                    .findById(taskId)
                    .orElseThrow();

            List<TaskEvent> history = taskEventRepository.findByTaskIdOrderByIdAsc(taskId);

            assertEquals(TaskStatus.IN_PROGRESS, reloadedTask.getStatus());

            assertEquals(initialVersion.longValue() + 1L, reloadedTask.getVersion().longValue());// `Long` 是对象，`long`
                                                                                                 // 是基础数值。我们要做数字加减运算（`+1L`），要用基础类型
                                                                                                 // long 来计算。

            assertEquals(1, history.size());

            TaskEvent startedEvent = history.get(0);

            assertEquals(TaskEventType.STARTED, startedEvent.getEventType());

            assertEquals(TaskStatus.TODO, startedEvent.getFromStatus());
            assertEquals(TaskStatus.IN_PROGRESS, startedEvent.getToStatus());

            Throwable failure = failures.get(0);

            assertInstanceOf(ObjectOptimisticLockingFailureException.class, failure);

        } finally {
            executor.shutdownNow();// 尝试中断正在运行的任务

        }
    }

}
