package com.dumptruckman.taskmin;

import com.sun.istack.internal.Nullable;
import org.jetbrains.annotations.NotNull;

public class TaskManager {

    private static final long DEFAULT_PRECISION_CHECK_TIME_MS = 10000;

    public static TaskManager createBasicTaskManager() {
        return new TaskManager(new MemoryTaskStore(), new TaskRunner() {}, DEFAULT_PRECISION_CHECK_TIME_MS);
    }

    @NotNull
    private final TaskStore taskStore;
    @NotNull
    private final TaskRunner taskRunner;
    private final long precisionCheckTimeMs;

    @Nullable
    private Task nextTask;

    private TaskManager(@NotNull TaskStore taskStore, @NotNull TaskRunner taskRunner, long precisionCheckTimeMs) {
        this.taskStore = taskStore;
        this.taskRunner = taskRunner;
        this.precisionCheckTimeMs = precisionCheckTimeMs;
    }



}
