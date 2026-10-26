package com.example.task_api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_api.dto.CreateTaskRequest;
import com.example.task_api.dto.PageResponse;
import com.example.task_api.dto.TaskResponse;
import com.example.task_api.dto.UpdateTaskRequest;
import com.example.task_api.model.Task;
import com.example.task_api.model.TaskStatus;
import com.example.task_api.service.TaskService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Null;

@RestController
@Validated
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public PageResponse<TaskResponse> tasks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "status", required = false) TaskStatus statusFilter,

            @RequestParam(name = "page", defaultValue = "0") @Min(value = 0, message = "page不能小于0") int page,

            @RequestParam(name = "size", defaultValue = "10") @Min(value = 1, message = "size必须大于0") @Max(value = 100, message = "size不能大于100") int size,

            @RequestParam(name = "sort", required = false) @Null(message = "当前接口不支持sort参数") String sort) {

        long ownerId = currentUserId(jwt);
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskService.getTasks(ownerId, statusFilter, pageable);

        Page<TaskResponse> responses = tasks.map(TaskResponse::new);
        return new PageResponse<>(responses);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long id) {
        Task task = taskService.findTaskById(currentUserId(jwt), id);

        return ResponseEntity.ok(new TaskResponse(task));
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateTaskRequest request) {
        long ownerId = currentUserId(jwt);

        Task createdTask = taskService.createTask(
                ownerId,
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getDeadline());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new TaskResponse(createdTask));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long id) {
        taskService.deleteTask(currentUserId(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<Void> updateTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long id,
            @Valid @RequestBody UpdateTaskRequest request) {

        taskService.updateTask(currentUserId(jwt), id, request.getTitle(), request.getDescription(),
                request.getVersion());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/{id}/start")
    public ResponseEntity<TaskResponse> startTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long id) {

        Task task = taskService.startTask(currentUserId(jwt), id);
        return ResponseEntity.ok(new TaskResponse(task));
    }

    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<TaskResponse> completeTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long id) {
        Task task = taskService.completeTask(currentUserId(jwt), id);
        return ResponseEntity.ok(new TaskResponse(task));
    }

    private long currentUserId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }

}