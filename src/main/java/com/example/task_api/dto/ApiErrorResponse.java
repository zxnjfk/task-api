package com.example.task_api.dto;
import java.time.Instant;

public class ApiErrorResponse {
    private int status;
    private String message;
    private String path;
    private String code;
    private Instant timestamp;

    public ApiErrorResponse(int status,String code,String message,String path){
        this.status=status;
        this.code=code;
        this.message=message;
        this.path=path;
        this.timestamp=Instant.now();
    }
    public int getStatus(){
        return this.status;
    }

    public String getMessage(){
        return this.message;
    }

    public String getPath(){
        return this.path;
    }

    public String getCode(){
        return code;
    }

    public Instant getTimestamp(){
        return timestamp;
    }
}
