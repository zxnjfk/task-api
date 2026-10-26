package com.example.task_api.service;

import com.example.task_api.model.Priority;
import com.example.task_api.model.Task;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import com.example.task_api.repository.TaskRepository;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import com.example.task_api.model.TaskStatus;
import org.springframework.data.domain.Sort;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.example.task_api.exception.TaskNotFoundException;

import com.example.task_api.model.TaskEvent;
import com.example.task_api.model.TaskEventType;
import com.example.task_api.repository.TaskEventRepository;
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskEventRepository taskEventRepository;

    public TaskService(TaskRepository taskRepository,TaskEventRepository taskEventRepository) {
        this.taskRepository=taskRepository;
        this.taskEventRepository=taskEventRepository;

    }
    @Transactional
    public Task createTask(long ownerId,String title,String description,Priority priority,LocalDate deadline){
        Task task=new Task(ownerId,title,description,priority,deadline);
        return taskRepository.save(task);
    }


    @Transactional (readOnly = true)
    public Task findTaskById(long ownerId,long id){
        return requireOwnedTask(ownerId, id);
    }

    @Transactional
    public void updateTask(
        long ownerId,
        long id,
        String newTitle,
        String newDescription,
        Long expectedVersion
    ){
        Task task = requireOwnedTask(ownerId, id);

        if(!task.getVersion().equals(expectedVersion)){
            throw new ObjectOptimisticLockingFailureException(Task.class, id);
        }
        task.updateDetails(newTitle, newDescription);
    }

    @Transactional
    public Task startTask(long ownerId,long id){
        Task task= requireOwnedTask(ownerId, id);

        TaskStatus fromStatus = task.getStatus();

        task.start();

        TaskEvent taskEvent = new TaskEvent(task.getId(), fromStatus, task.getStatus(), TaskEventType.STARTED);

        taskEventRepository.save(taskEvent);

        return task;
    }

    @Transactional
    public Task completeTask(long ownerId,long id){
        Task task=requireOwnedTask(ownerId, id);

        TaskStatus fromStatus = task.getStatus();

        task.complete();


        TaskEvent taskEvent = new TaskEvent(task.getId(), fromStatus, task.getStatus(), TaskEventType.COMPLETED);

        taskEventRepository.save(taskEvent);

        return task;

    }

    @Transactional
    public void deleteTask(long ownerId,long id){
        Task task = requireOwnedTask(ownerId, id);
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public Page<Task> getTasks(long ownerId,Pageable pageable){
        Pageable pageOnly =PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize()
        );
        return taskRepository.findAllByOwnerIdOrderByBusinessPriority(ownerId,pageOnly);
    }

    @Transactional(readOnly = true)
    public Page<Task> getTasks(long ownerId,TaskStatus statusFilter,Pageable pageable){
        if(statusFilter==null){
            return getTasks(ownerId,pageable);

        }
        Pageable pageById=PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by("id").ascending()
        );

        return taskRepository.findByOwnerIdAndStatus(ownerId,statusFilter, pageById);
    }

    private Task requireOwnedTask(long ownerId, long id){
        return taskRepository.findByOwnerIdAndId(ownerId,id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

}
