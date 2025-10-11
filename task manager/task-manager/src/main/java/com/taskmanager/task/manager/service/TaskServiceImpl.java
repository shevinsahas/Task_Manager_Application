package com.taskmanager.task.manager.service;

import com.taskmanager.task.manager.dto.TaskMapper;
import com.taskmanager.task.manager.dto.TaskRequest;
import com.taskmanager.task.manager.dto.TaskResponse;
import com.taskmanager.task.manager.model.Task;
import com.taskmanager.task.manager.repository.TaskRepository;
import com.taskmanager.task.manager.util.TaskSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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

    @Transactional(readOnly = true)
    public Page<TaskResponse> search(String q, String view, Pageable pageable) {
        Set<Task.Status> statuses = switch (view == null ? "ALL" : view.toUpperCase()) {
            case "ACTIVE" -> EnumSet.of(Task.Status.TODO, Task.Status.IN_PROGRESS);
            case "COMPLETED" -> EnumSet.of(Task.Status.DONE);
            default -> EnumSet.noneOf(Task.Status.class); // ALL = no filter
        };

        Specification<Task> spec = Specification.allOf(
                TaskSpecs.textLike(q),
                TaskSpecs.statusIn(statuses.isEmpty() ? null : statuses));

        return repo.findAll(spec, pageable).map(TaskMapper::toResponse);
    }

    public void clearCompleted() {
        repo.deleteByStatus(Task.Status.DONE);
    }

    public TaskResponse updateNote(UUID id, String note) {
        Task t = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
        t.setNote(note);
        return TaskMapper.toResponse(t);
    }
}

