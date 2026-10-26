package com.example.task_api.exception;

public class NotificationNotFoundException extends RuntimeException{

    public NotificationNotFoundException(long id){
        super("Notification not found, id=" + id);
    }
}
