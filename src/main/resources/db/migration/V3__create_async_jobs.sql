CREATE TABLE async_jobs(
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL ,
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    started_at DATETIME(6),
    finished_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),

    INDEX idx_async_jobs_task_id_id(task_id,id)

)ENGINE=InnoDB
 DEFAULT CHARSET=utf8mb4
 COLLATE=utf8mb4_0900_ai_ci;