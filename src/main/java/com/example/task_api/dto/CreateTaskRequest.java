package com.example.task_api.dto;

import com.example.task_api.model.Priority;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public class CreateTaskRequest{
    @NotBlank(message="任务标题不能为空")
    @Size(max=200,message="任务标题不能超过200个字符")
    private String title;

    private String description;
    @NotNull(message="任务优先级不能为空")
    private Priority priority;

    @NotNull(message = "任务截止日期不能为空")
    private LocalDate deadline;

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title=title;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description=description;
    }
    public Priority getPriority(){
        return priority;
    }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }




}