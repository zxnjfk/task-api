package com.example.task_api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.task_api.model.AsyncJob;
import com.example.task_api.model.AsyncJobStatus;
import com.example.task_api.model.AsyncJobType;
import com.example.task_api.model.ReminderStage;

public interface AsyncJobRepository
                extends JpaRepository<AsyncJob, Long> {

        Optional<AsyncJob> findByTaskIdAndJobTypeAndReminderStageAndTargetDeadline(
                        Long taskId,
                        AsyncJobType jobType,
                        ReminderStage reminderStage,
                        LocalDate targetDeadline);

        @Query("""
                            select job.id
                            from AsyncJob job
                            where job.status = :status
                            order by job.id asc
                        """)
        List<Long> findIdsByStatus(
                        @Param("status") AsyncJobStatus status,
                        Pageable pageable);

}