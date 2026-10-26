package com.example.task_api.exception;

public class TaskNotFoundException extends RuntimeException{

    public TaskNotFoundException(long id){
        super("Task 不存在,id="+id);
    }
}
