package com.example.task_api.controller;

import java.time.LocalDate;

import com.example.task_api.model.Priority;
import com.example.task_api.model.Task;
import com.example.task_api.repository.TaskRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.example.task_api.support.TestUserFactory.saveUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.example.task_api.model.AppUser;
import com.example.task_api.repository.AppUserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskOptimisticLockIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @AfterEach
    void cleanUp() {
        taskRepository.deleteAll();
    }

    @Test
    void staleVersionShouldReturnConflictAndNotOverwriteFirstUpdate()
            throws Exception {
        AppUser user = saveUser(appUserRepository);

        Task preparedTask = taskRepository.saveAndFlush(
                new Task(
                        user.getId(),
                        "乐观锁测试任务",
                        "原始描述",
                        Priority.HIGH,
                        LocalDate.of(2026, 9, 30)));// 如果你只用`save()`，没 flush，insert 还没真正跑到数据库，version 还没被数据库 / JPA
                                                    // 生成，拿到的 version 就是 null，后面整个乐观锁测试直接全部报错。

        Long taskId = preparedTask.getId();
        Long oldVersion = preparedTask.getVersion();

        assertNotNull(oldVersion);
        assertNotNull(taskId);

        mockMvc.perform(
                get("/tasks/{id}", taskId)
                        .with(jwt().jwt(jwt -> jwt.subject(user.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(oldVersion.intValue()));

        String firstRequest = """
                {
                  "title": "第一次修改成功",
                  "description": "使用当前版本修改",
                  "version": %d
                }
                """.formatted(oldVersion);

        mockMvc.perform(
                put("/tasks/{id}", taskId)
                        .with(jwt().jwt(jwt -> jwt.subject(user.getId().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest))
                .andExpect(status().isNoContent());

        Task afterFirstUpdate = taskRepository
                .findById(taskId)
                .orElseThrow();

        assertEquals("第一次修改成功", afterFirstUpdate.getTitle());
        assertEquals("使用当前版本修改", afterFirstUpdate.getDescription());

        Long versionAfterFirstUpdate = afterFirstUpdate.getVersion();

        assertEquals(oldVersion + 1, versionAfterFirstUpdate);

        String staleRequest = """
                {
                   "title": "旧请求试图覆盖",
                   "description": "这个内容不能进入数据库",
                   "version": %d
                }
                """.formatted(oldVersion);

        mockMvc.perform(
                put("/tasks/{id}", taskId)
                        .with(jwt().jwt(jwt -> jwt.subject(user.getId().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(staleRequest)

        )

                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.message").value(
                                "任务已被其他请求修改，请刷新后重试"));

        Task afterConflict = taskRepository
                .findById(taskId)
                .orElseThrow();

        assertEquals("第一次修改成功", afterConflict.getTitle());

        assertEquals("使用当前版本修改", afterConflict.getDescription());

        assertEquals(versionAfterFirstUpdate, afterConflict.getVersion());

    }

}