package com.dumptruckman.taskmin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class MemoryTaskStore implements TaskStore {

    private final SortedSet<Task> taskList = new ConcurrentSkipListSet<>();
    private volatile int nextId = 0;

    @Override
    @Nullable
    public Task getNextTask() {
        if (taskList.isEmpty()) {
            return null;
        }
        return taskList.first();
    }

    @Override
    public void markCompleted(@NotNull Task task) {
        taskList.remove(task);
    }

    @Override
    public @NotNull Task createTask(@NotNull TaskBuilder taskBuilder) {
        Task task;
        synchronized (this) {
            task = taskBuilder.build(nextId);
            taskList.add(task);
            nextId++;
        }
        return task;
    }
}
