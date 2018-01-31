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

import java.time.LocalDateTime;
import java.util.Objects;

public class Task implements Runnable, Comparable<Task> {

    @NotNull
    public static TaskBuilder builder(@NotNull Runnable action) {
        return new TaskBuilder(action);
    }

    private final int taskId;
    @NotNull
    private final Runnable action;
    @NotNull
    private volatile LocalDateTime executionTime;

    public Task(int taskId, @NotNull Runnable action, @NotNull LocalDateTime executionTime) {
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
    @NotNull
    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    /**
     * Sets the execution time of this task.
     *
     * @param executionTime the new execution time.
     */
    void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }

    @Override
    public int compareTo(@NotNull Task o) {
        return getExecutionTime().compareTo(o.getExecutionTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return getTaskId() == task.getTaskId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTaskId());
    }
}
