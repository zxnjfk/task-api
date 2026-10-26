package com.example.task_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.task_api.config.WebSecurityConfiguration;
import com.example.task_api.security.RestAuthenticationEntryPoint;
import com.example.task_api.service.NotificationService;

@WebMvcTest(NotificationController.class)
@Import({
        WebSecurityConfiguration.class,
        RestAuthenticationEntryPoint.class,
        NotificationControllerTest.SecurityTestConfiguration.class
})
class NotificationControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    static class SecurityTestConfiguration {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldUseJwtSubjectAsRecipientIdWhenListingNotifications()
            throws Exception {
        when(notificationService.getNotifications(
                eq(42L),
                any(Pageable.class)
        )).thenReturn(Page.empty());//如果不返回合法但没有数据的分页对象，那么执行 notifications.map(...)时触发 NullPointerException


        mockMvc.perform(
                get("/notifications")
                        .with(jwt().jwt(jwt -> jwt.subject("42")))
        ).andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);//参数捕获器

        verify(notificationService).getNotifications(
                eq(42L),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }
}
