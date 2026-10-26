package com.example.task_api.repository;

import com.example.task_api.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.task_api.model.TaskStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaskRepository
       extends JpaRepository<Task,Long>{

         @Query(
            value="""
                  select t
                  from Task t
                  where t.ownerId = :ownerId
                  order by
                     case t.priority
                         when com.example.task_api.model.Priority.HIGH then 1
                         when com.example.task_api.model.Priority.MEDIUM then 2
                         when com.example.task_api.model.Priority.LOW then 3
                         else 4
                     end,
                     t.id asc
                  """,
               countQuery = """
                     select count(t)
                     from Task t
                     where t.ownerId = :ownerId
                     """

         )
         Page<Task> findAllByOwnerIdOrderByBusinessPriority(@Param("ownerId")Long ownerId,Pageable pageable);

         Page<Task> findByOwnerIdAndStatus(Long ownerId,TaskStatus statusFilter,Pageable pageable);

         List<Task> findByDeadlineAndStatusIn(
            LocalDate deadline,
            Collection<TaskStatus> statuses
         );

         Optional<Task> findByOwnerIdAndId(
                 Long ownerId,
                 Long id
         );

    }
