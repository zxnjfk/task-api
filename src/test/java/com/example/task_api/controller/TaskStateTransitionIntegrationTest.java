package com.example.task_api.controller;

import com.example.task_api.repository.TaskRepository;

import org.junit.jupiter.api.AfterEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;

import com.example.task_api.model.Priority;
import com.example.task_api.model.Task;
import com.example.task_api.model.TaskStatus;
import com.example.task_api.model.AppUser;
import com.example.task_api.repository.AppUserRepository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.example.task_api.support.TestUserFactory.saveUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskStateTransitionIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @AfterEach
    void cleanUp(){
        taskRepository.deleteAll();
    }

    @Test
    void completedTaskShouldReturnConflictWhenStartedAgain()
            throws Exception{

        AppUser user = saveUser(appUserRepository);
        Task task = new Task(
             user.getId(),
             "已完成任务",
             "测试非法状态转换",
             Priority.HIGH,
             LocalDate.of(2026, 9, 30)
        );

        task.start();
        task.complete();

        Task savedTask = taskRepository.saveAndFlush(task);

        mockMvc.perform(
                post("/tasks/{id}/start",savedTask.getId())
                        .with(jwt().jwt(jwt -> jwt.subject(user.getId().toString())))
            )//模拟发送 `POST /tasks/{id}/start` 请求
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.code").value("INVALID_TASK_STATE")
            )
            .andExpect(
                jsonPath("$.path").value("/tasks/"+savedTask.getId()+"/start")
            );

            Task unchangedTask=taskRepository
                .findById(savedTask.getId())
                .orElseThrow();

            assertEquals(unchangedTask.getStatus(), TaskStatus.COMPLETED);

            }
}
