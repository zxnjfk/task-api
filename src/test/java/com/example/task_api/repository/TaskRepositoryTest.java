package com.example.task_api.repository;

import java.time.LocalDate;
import java.util.List;


import com.example.task_api.model.Priority;
import com.example.task_api.model.Task;
import com.example.task_api.model.TaskStatus;
import com.example.task_api.model.AppUser;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.example.task_api.support.TestUserFactory.saveUser;

import org.springframework.test.context.ActiveProfiles;
@DataJpaTest
@ActiveProfiles ("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AppUserRepository appUserRepository;


    @Test
    void shouldSaveAndFindTask(){
        AppUser user = saveUser(appUserRepository);
        Task task=new Task(
            user.getId(),
             "学习Repository测试",
             "验证JPA字段映射",
             Priority.HIGH,
             LocalDate.of(2026, 9, 10)
        );

        Task savedTask=taskRepository.saveAndFlush(task);

        assertNotNull(savedTask.getId());

         /*
         * 清除当前持久化上下文。
         * 否则下一次查询可能直接从 Hibernate 一级缓存取对象，
         * 无法证明数据库 SELECT 和字段映射正常。
         */

         entityManager.clear();
         Task foundTask=taskRepository.findById(savedTask.getId()).orElseThrow();

         assertEquals("学习Repository测试", foundTask.getTitle());
         assertEquals(Priority.HIGH, foundTask.getPriority());
         assertEquals(LocalDate.of(2026,9,10), foundTask.getDeadline());
         assertEquals(user.getId(), foundTask.getOwnerId());
    }

    @Test
    void shouldFindIncompleteTasksDueOnSpecifiedDate(){
        LocalDate targetDeadline = LocalDate.of(2026, 9, 30);

        AppUser user = saveUser(appUserRepository);
        Long ownerId = user.getId();

        Task todoTask = new Task(
            ownerId,
            "TODO task",
            "Should be selected",
            Priority.MEDIUM,
            targetDeadline
        );

        Task inProgressTask = new Task(
            ownerId,
            "In-progress task",
            "Should be selected",
            Priority.MEDIUM,
            targetDeadline
        );
        inProgressTask.start();

        Task completedTask = new Task(
            ownerId,
            "Completed task",
            "Should not be selected",
            Priority.MEDIUM,
            targetDeadline
        );
        completedTask.start();
        completedTask.complete();

        Task differentDeadlineTask = new Task(
            ownerId,
            "Different deadline task",
            "Should not be selected",
            Priority.MEDIUM,
            targetDeadline.plusDays(1)
        );

        taskRepository.saveAllAndFlush(
            List.of(
                    todoTask,
                    inProgressTask,
                    completedTask,
                    differentDeadlineTask
            )
        );

        List<Task> result = taskRepository.findByDeadlineAndStatusIn(
            targetDeadline,
            List.of(TaskStatus.TODO,TaskStatus.IN_PROGRESS)
        );
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(
            task -> task.getId().equals(todoTask.getId())
        ));

        assertTrue(result.stream().anyMatch(
            task -> task.getId().equals(inProgressTask.getId())
        ));
    }
}
