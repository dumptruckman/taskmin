package com.dumptruckman.taskmin;

import org.jetbrains.annotations.NotNull;

public interface TaskRunner {

    /**
     * Runs the given task. This or the task run should never add more tasks or there will be a deadlock.
     */
    default void runTask(@NotNull Task task) {
        task.run();
    }
}
