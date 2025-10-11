package com.taskmanager.task.manager.controller;

import com.taskmanager.task.manager.dto.TaskRequest;
import com.taskmanager.task.manager.dto.TaskResponse;
import com.taskmanager.task.manager.model.Task;
import com.taskmanager.task.manager.service.TaskService;
import com.taskmanager.task.manager.service.TaskServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;

    private final TaskServiceImpl serviceImpl;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest request) {
        return service.create(request);
    }


    @GetMapping
    public Page<TaskResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "ALL") String view,        // ALL | ACTIVE | COMPLETED
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt,DESC") String sort
    ) {
        String[] s = sort.split(",");
        Sort by = s.length == 2 ? Sort.by(Sort.Direction.fromString(s[1]), s[0]) : Sort.by("updatedAt").descending();
        Pageable pageable = PageRequest.of(page, size, by);
        return serviceImpl.search(q, view, pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable UUID id) { return service.get(id); }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable UUID id, @Valid @RequestBody TaskRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(@PathVariable UUID id, @RequestParam Task.Status status) {
        TaskResponse current = service.get(id);
        TaskRequest req = new TaskRequest(
                current.title(),
                current.description(),
                status,
                current.priority(),
                current.dueDate(),
                current.note()
        );
        return service.update(id, req);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.delete(id); }


    @PatchMapping("/{id}/note")
    public TaskResponse updateNote(@PathVariable UUID id, @RequestBody String note) {
        return serviceImpl.updateNote(id, note);
    }


    @DeleteMapping("/completed")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCompleted() {
        serviceImpl.clearCompleted();
    }
}
