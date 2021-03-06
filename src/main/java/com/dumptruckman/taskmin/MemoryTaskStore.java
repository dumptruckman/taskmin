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

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A simple in memory TaskStore.
 */
class MemoryTaskStore implements TaskStore {

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
    public void markRepeated(@NotNull Task task) {
        // The task must be resorted as the execution time has changed.
        taskList.remove(task);
        taskList.add(task);
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
