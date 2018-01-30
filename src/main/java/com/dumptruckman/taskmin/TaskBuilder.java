package com.dumptruckman.taskmin;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class TaskBuilder {

    @NotNull
    private final Runnable action;
    @NotNull
    private LocalDateTime executionTime = LocalDateTime.now();

    TaskBuilder(@NotNull Runnable action) {
        this.action = action;
    }

    @NotNull
    public TaskBuilder executeAt(@NotNull LocalDateTime executionTime) {
        this.executionTime = executionTime;
        return this;
    }

    @NotNull
    public Task build(int taskId) {
        return new Task(taskId, action, executionTime);
    }
}
