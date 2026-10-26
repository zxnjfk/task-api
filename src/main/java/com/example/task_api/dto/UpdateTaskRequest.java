package com.example.task_api.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public class UpdateTaskRequest {
    @NotBlank(message="任务标题不能为空")
    @Size(max=200,message="任务标题不能超过200个字符")
    private String title;

    private String description;

    @NotNull(message="任务版本号不能为空")
    private Long version;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getVersion(){
        return version;
    }

    public void setVersion(Long version){
        this.version=version;
    }


}