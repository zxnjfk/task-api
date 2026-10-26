package com.example.task_api.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import com.example.task_api.model.AsyncJob;
import com.example.task_api.model.Notification;
import com.example.task_api.model.NotificationType;
import com.example.task_api.model.Task;
import com.example.task_api.repository.NotificationRepository;
import com.example.task_api.exception.NotificationNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Notification getOrCreateTaskReminderNotification(
            AsyncJob job,
            Task task) {
        Optional<Notification> existingNotification = notificationRepository.findByAsyncJobId(job.getId());

        if (existingNotification.isPresent()) {
            return existingNotification.get();
        }

        String content = "Task \""
                + task.getTitle()
                + "\" is due on "
                + job.getTargetDeadline();

        Notification notification = new Notification(
                task.getOwnerId(),
                task.getId(),
                job.getId(),
                NotificationType.TASK_REMINDER,
                content);

        return notificationRepository.save(notification);

    }

    @Transactional (readOnly = true)
    public Page<Notification> getNotifications(
            long recipientId,
            Pageable pageable
    ) {
        Pageable pageOnly = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDescIdDesc(
                       recipientId,
                       pageOnly
                );
    }

    @Transactional
    public Notification markAsRead(
            long recipientId,
            long notificationId
    ){
       Notification notification = requireOwnedNotification(recipientId, notificationId);

       notification.markAsRead();

       return notification;
    }

    private Notification requireOwnedNotification(
        long recipientId,
        long notificationId
    ){
        return notificationRepository
                .findByRecipientIdAndId(
                        recipientId,
                        notificationId
                )
                .orElseThrow(()-> new NotificationNotFoundException(notificationId)
                );
    }

}
