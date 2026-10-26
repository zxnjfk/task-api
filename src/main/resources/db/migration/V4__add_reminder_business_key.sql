ALTER TABLE async_jobs
    ADD COLUMN reminder_stage VARCHAR(30) NOT NULL AFTER job_type,
    ADD COLUMN target_deadline DATE NOT NULL AFTER reminder_stage,
    ADD CONSTRAINT uk_async_jobs_reminder_business_key
        UNIQUE (
            task_id,
            job_type,
            reminder_stage,
            target_deadline
        );