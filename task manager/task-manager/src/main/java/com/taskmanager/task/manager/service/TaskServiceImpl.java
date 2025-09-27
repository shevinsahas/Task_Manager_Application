package com.taskmanager.task.manager.service;

import com.taskmanager.task.manager.dto.TaskMapper;
import com.taskmanager.task.manager.dto.TaskRequest;
import com.taskmanager.task.manager.dto.TaskResponse;
import com.taskmanager.task.manager.model.Task;
import com.taskmanager.task.manager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repo;

    @Override
    public TaskResponse create(TaskRequest request) {
        Task saved = repo.save(TaskMapper.toEntity(request));
        return TaskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse update(UUID id, TaskRequest request) {
        Task existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
        TaskMapper.updateEntity(existing, request);
        // @PreUpdate will update timestamps
        return TaskMapper.toResponse(existing);
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Task not found: " + id);
        }
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse get(UUID id) {
        return repo.findById(id)
                .map(TaskMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> list() {
        return repo.findAll().stream().map(TaskMapper::toResponse).toList();
    }
}

