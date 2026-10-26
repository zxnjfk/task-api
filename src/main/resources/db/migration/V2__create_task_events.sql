CREATE TABLE task_events(
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    from_status VARCHAR(20) NOT NULL,
    to_status VARCHAR(20) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_task_events_task_id_id(task_id,id)
)ENGINE=InnoDB
 DEFAULT CHARSET=utf8mb4
 COLLATE=utf8mb4_0900_ai_ci;