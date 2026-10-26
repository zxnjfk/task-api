package com.example.task_api.exception;

public class AsyncJobNotFoundException extends RuntimeException{

    public AsyncJobNotFoundException(long id){
        super("AsyncJob not found, id=" + id);
    }
}
