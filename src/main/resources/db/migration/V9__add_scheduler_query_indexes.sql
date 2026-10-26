CREATE INDEX idx_tasks_deadline_status
    ON tasks (deadline, status);

CREATE INDEX idx_async_jobs_status_id
    ON async_jobs (status, id);
