/*
 * Copyright 2018 dumptruckman
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.dumptruckman.taskmin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;

public class TaskBuilder {

    @NotNull
    private final Runnable action;
    @NotNull
    private LocalDateTime executionTime = LocalDateTime.now();
    @Nullable
    private Duration period = null;
    private boolean skipFirstExecution = false;

    TaskBuilder(@NotNull Runnable action) {
        this.action = action;
    }

    /**
     * Specifies the time at which the task should be executed.
     */
    @NotNull
    public TaskBuilder executeAt(@NotNull LocalDateTime executionTime) {
        this.executionTime = executionTime;
        return this;
    }

    /**
     * After the first execution, this is how long the task will wait before repeating. The task will not repeat
     * if this is not specified.
     */
    @NotNull
    public TaskBuilder repeatEvery(@NotNull Duration period) {
        this.period = period;
        return this;
    }

    public TaskBuilder skipFirstExecution() {
        this.skipFirstExecution = true;
        return this;
    }

    @NotNull
    public Task build(int taskId) {
        if (skipFirstExecution && period == null) {
            throw new IllegalStateException("A repeat period must be specified to skip the first execution.");
        }

        Task task;
        if (period != null) {
            task = new RepeatingTask(taskId, action, executionTime, period);
            if (skipFirstExecution) {
                task.setExecutionTime(LocalDateTime.now().plus(period));
            }
        } else {
            task = new Task(taskId, action, executionTime);
        }

        return task;
    }
}
