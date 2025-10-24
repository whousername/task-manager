package org.example.taskmanager.tasks;

public record TaskSearchFilter(
        Long creatorId,
        Long assignedUserId,
        Status status,
        Priority priority,
        Integer pageSize,
        Integer pageNum
) {}
