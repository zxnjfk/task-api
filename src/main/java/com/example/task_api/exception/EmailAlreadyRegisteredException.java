package com.example.task_api.exception;

public class EmailAlreadyRegisteredException extends RuntimeException{

    public EmailAlreadyRegisteredException(){
        super("Email is already registered");
    }
}
