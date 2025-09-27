package com.taskmanager.task.manager.dto;

import com.taskmanager.task.manager.model.Task;

public class TaskMapper {

    public static Task toEntity(TaskRequest req) {
        return Task.builder()
                .title(req.title())
                .description(req.description())
                .status(req.status())
                .priority(req.priority())
                .dueDate(req.dueDate())
                .build();
    }

    public static void updateEntity(Task entity, TaskRequest req) {
        entity.setTitle(req.title());
        entity.setDescription(req.description());
        entity.setStatus(req.status());
        entity.setPriority(req.priority());
        entity.setDueDate(req.dueDate());
    }

    public static TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(), t.getTitle(), t.getDescription(),
                t.getStatus(), t.getPriority(), t.getDueDate(),
                t.getCreatedAt(), t.getUpdatedAt()
        );
    }
}

