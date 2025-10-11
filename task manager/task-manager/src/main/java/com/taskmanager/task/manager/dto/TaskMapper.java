package com.taskmanager.task.manager.dto;

import com.taskmanager.task.manager.model.Task;

public final class TaskMapper {

    private TaskMapper() {}

    public static Task toEntity(TaskRequest r) {
        return Task.builder()
                .title(r.title())
                .description(r.description())
                .status(r.status())
                .priority(r.priority())
                .dueDate(r.dueDate())
                .note(r.note())
                .build();
    }

    public static void updateEntity(Task t, TaskRequest r) {
        t.setTitle(r.title());
        t.setDescription(r.description());
        t.setStatus(r.status());
        t.setPriority(r.priority());
        t.setDueDate(r.dueDate());
        t.setNote(r.note());

    }

    public static TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getDueDate(),
                t.getNote(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}


