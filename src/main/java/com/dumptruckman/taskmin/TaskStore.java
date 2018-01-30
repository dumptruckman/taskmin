package com.dumptruckman.taskmin;

import com.sun.istack.internal.Nullable;
import org.jetbrains.annotations.NotNull;

public interface TaskStore {

    /**
     * Retrieves the task that will be the first to expire or null if no tasks remain.
     */
    @Nullable
    Task getNextTask();

    /**
     * Notifies the backing store that the given task has been completed.
     */
    void markCompleted(@NotNull Task task);

    /**
     * Creates the task from the given TaskBuilder and adds it to the task store.
     *
     * @return The newly created Task.
     */
    @NotNull
    Task createTask(@NotNull TaskBuilder taskBuilder);
}
