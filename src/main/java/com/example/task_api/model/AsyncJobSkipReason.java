package com.example.task_api.model;

public enum AsyncJobSkipReason {
    TASK_NOT_FOUND,
    TASK_NOT_REMINDABLE,
    DEADLINE_CHANGED,
    REMINDER_WINDOW_MISSED
}
