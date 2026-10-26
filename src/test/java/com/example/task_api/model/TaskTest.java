package com.example.task_api.model;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TaskTest {

     @Test
     void todoTaskShouldStart(){
        Task task=new Task(
            100L,
            "学习状态机",
            "测试任务开始",
            Priority.HIGH,
            LocalDate.of(2026,9,30)
        );

        task.start();
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());

     }

     @Test
     void inProgressTaskShouldComplete(){
        Task task=new Task(
            100L,
            "学习状态机",
            "测试任务开始",
            Priority.HIGH,
            LocalDate.of(2026,9,30)
        );
        task.start();
        task.complete();

        assertEquals(TaskStatus.COMPLETED, task.getStatus());

     }
}