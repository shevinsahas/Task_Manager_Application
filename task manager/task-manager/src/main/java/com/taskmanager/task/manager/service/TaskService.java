package com.taskmanager.task.manager.service;

import com.taskmanager.task.manager.dto.TaskRequest;
import com.taskmanager.task.manager.dto.TaskResponse;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse create(TaskRequest request);
    TaskResponse update(UUID id, TaskRequest request);
    void delete(UUID id);
    TaskResponse get(UUID id);
    List<TaskResponse> list();
}

