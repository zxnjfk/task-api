package com.example.task_api.exception;

import com.example.task_api.model.TaskStatus;
public class InvalidTaskStateException extends RuntimeException{

    public InvalidTaskStateException(
        TaskStatus currentStatus,
        TaskStatus targetStatus
    ){
        super(
            "Task 状态不能从"
            + currentStatus
            +" 转换为 "
            +targetStatus
        );
    }
}
