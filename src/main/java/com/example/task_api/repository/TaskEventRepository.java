package com.example.task_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_api.model.TaskEvent;
public interface TaskEventRepository
        extends JpaRepository<TaskEvent,Long>{

    List<TaskEvent> findByTaskIdOrderByIdAsc(Long taskId);

}
