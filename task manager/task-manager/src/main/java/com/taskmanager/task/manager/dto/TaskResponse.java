package com.taskmanager.task.manager.dto;

import com.taskmanager.task.manager.model.Task;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        Task.Status status,
        Task.Priority priority,
        LocalDate dueDate,
        String note,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

