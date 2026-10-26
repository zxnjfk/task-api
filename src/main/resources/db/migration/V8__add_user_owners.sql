ALTER TABLE tasks
   ADD COLUMN owner_id BIGINT NOT NULL AFTER id,
   ADD INDEX idx_tasks_owner_id_id (owner_id, id),
   ADD CONSTRAINT fk_tasks_owner_id
       FOREIGN KEY (owner_id)
       REFERENCES app_users(id)
       ON DELETE RESTRICT;

ALTER TABLE notifications
    ADD COLUMN recipient_id BIGINT NOT NULL AFTER id,
    ADD  INDEX idx_notifications_recipient_created_at_id (
        recipient_id,
        created_at,
        id
    ),
    ADD CONSTRAINT fk_notifications_recipient_id
        FOREIGN KEY (recipient_id)
        REFERENCES app_users(id)
        ON DELETE RESTRICT;
