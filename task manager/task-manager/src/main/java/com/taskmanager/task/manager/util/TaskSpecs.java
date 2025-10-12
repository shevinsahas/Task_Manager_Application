package com.taskmanager.task.manager.util;

import com.taskmanager.task.manager.model.Task;
import com.taskmanager.task.manager.model.Task.Status;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

public final class TaskSpecs {

    private TaskSpecs() {}

    public static Specification<Task> textLike(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("description")), like),
                cb.like(cb.lower(root.get("note")), like)
        );
    }

    public static Specification<Task> statusIn(Set<Status> statuses) {
        if (statuses == null || statuses.isEmpty()) return null;
        return (root, cq, cb) -> root.get("status").in(statuses);
    }
}

