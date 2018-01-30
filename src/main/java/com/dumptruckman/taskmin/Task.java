package com.dumptruckman.taskmin;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class Task implements Runnable, Comparable<Task> {

    @NotNull
    public static TaskBuilder builder(@NotNull Runnable action) {
        return new TaskBuilder(action);
    }

    private final int taskId;
    private final Runnable action;
    private final LocalDateTime executionTime;

    public Task(int taskId, Runnable action, LocalDateTime executionTime) {
        this.taskId = taskId;
        this.action = action;
        this.executionTime = executionTime;
    }

    @Override
    public void run() {
        action.run();
    }

    /**
     * Returns the ID of the task used by the associated {@link TaskStore}
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * Returns the LocalDateTime objects that represent when this task should be run.
     */
    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    @Override
    public int compareTo(@NotNull Task o) {
        return getExecutionTime().compareTo(o.getExecutionTime());
    }
}
