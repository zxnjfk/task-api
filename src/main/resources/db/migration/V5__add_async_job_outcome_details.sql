ALTER TABLE async_jobs
    ADD COLUMN skip_reason VARCHAR(50) NULL AFTER status,
    ADD COLUMN failure_type VARCHAR(50) NULL AFTER skip_reason;