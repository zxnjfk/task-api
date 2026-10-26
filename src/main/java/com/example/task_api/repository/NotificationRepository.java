package com.example.task_api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_api.model.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification,Long>{

    Optional<Notification> findByAsyncJobId(Long asyncJobId);

    Page<Notification> findByRecipientIdOrderByCreatedAtDescIdDesc(
           Long recipientId,
           Pageable pageable
    );

    Optional<Notification> findByRecipientIdAndId(
        Long recipientId,
        Long id
    );
}
