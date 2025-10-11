package com.taskmanager.task.manager.dto;

import com.taskmanager.task.manager.model.Task;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 4000) String description,
        @NotNull Task.Status status,
        @NotNull Task.Priority priority,
        LocalDate dueDate,
        @Size(max = 1000) String note
) {}
