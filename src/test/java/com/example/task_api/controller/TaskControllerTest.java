package com.example.task_api.controller;

import com.example.task_api.exception.TaskNotFoundException;
import com.example.task_api.config.WebSecurityConfiguration;
import com.example.task_api.security.RestAuthenticationEntryPoint;
import com.example.task_api.service.TaskService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(TaskController.class)
@Import({
        WebSecurityConfiguration.class,
        RestAuthenticationEntryPoint.class,
        TaskControllerTest.SecurityTestConfiguration.class
})
class TaskControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    static class SecurityTestConfiguration {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception{
        String json="""
            {
             "title":"",
             "description":"测试校验",
             "priority": "HIGH",
             "deadline": "2026-09-01"
        }
        """;

        mockMvc.perform(
                post("/tasks")
                .with(jwt().jwt(jwt -> jwt.subject("1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("任务标题不能为空") );

            verifyNoInteractions(taskService);

    }

    @Test
    void shouldReturnUnifiedNotFoundResponseWhenTaskDoesNotExist() throws Exception {
        when(taskService.findTaskById(1L, 999L))
                .thenThrow(new TaskNotFoundException(999L));

        mockMvc.perform(
                get("/tasks/{id}", 999L)
                        .with(jwt().jwt(jwt -> jwt.subject("1"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Task 不存在,id=999"))
            .andExpect(jsonPath("$.path").value("/tasks/999"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

}
