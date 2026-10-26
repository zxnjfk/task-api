package com.example.task_api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_api.dto.NotificationResponse;
import com.example.task_api.dto.PageResponse;
import com.example.task_api.model.Notification;
import com.example.task_api.service.NotificationService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Null;

@RestController
@RequestMapping("/notifications")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ){
        this.notificationService=notificationService;
    }

    @GetMapping
    public PageResponse<NotificationResponse> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            )
            @Min(value = 0,message = "page不能小于0")
            int page,
            @RequestParam(
                    name = "size",
                    defaultValue = "10"
            )
            @Min(value = 1, message = "size必须大于0")
            @Max(value = 100, message = "size不能大于100")
            int size,
            @RequestParam (
                    name = "sort",
                    required = false
            )
            @Null(message = "当前接口不支持sort参数")
            String sort
    ){
        Pageable pageable = PageRequest.of(page,size);

        Page<Notification> notifications =
                notificationService.getNotifications(
                    currentUserId(jwt),
                    pageable
                );

        Page<NotificationResponse> responses =
               notifications.map(NotificationResponse::new);

        return new PageResponse<>(responses);
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long id
    ){
        Notification notification = notificationService.markAsRead(
               currentUserId(jwt),
               id
        );
        return new NotificationResponse(notification);


    }

    private long currentUserId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}
