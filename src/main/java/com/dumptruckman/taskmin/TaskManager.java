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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TaskManager {

    private static final long DEFAULT_PRECISION_CHECK_TIME_MS = 10000;

    public static TaskManager createBasicTaskManager() {
        return new TaskManager(new MemoryTaskStore(), new TaskRunner() {}, DEFAULT_PRECISION_CHECK_TIME_MS);
    }

    public static TaskManager createTaskManager(@NotNull TaskStore taskStore, @NotNull TaskRunner taskRunner) {
        return new TaskManager(taskStore, taskRunner, DEFAULT_PRECISION_CHECK_TIME_MS);
    }

    public static TaskManager createTaskManager(@NotNull TaskStore taskStore, @NotNull TaskRunner taskRunner,
                                                long precisionCheckTimeMs) {
        return new TaskManager(taskStore, taskRunner, precisionCheckTimeMs);
    }

    @NotNull
    private final TaskStore taskStore;
    @NotNull
    private final TaskRunner taskRunner;
    private final long precisionCheckTimeMs;
    @NotNull
    private final TimeKeeper timeKeeper;

    @Nullable
    private Task nextTask;

    private TaskManager(@NotNull TaskStore taskStore, @NotNull TaskRunner taskRunner, long precisionCheckTimeMs) {
        this.taskStore = taskStore;
        this.taskRunner = taskRunner;
        this.precisionCheckTimeMs = precisionCheckTimeMs;
        this.timeKeeper = new TimeKeeper(this);

        timeKeeper.setDaemon(true);
        timeKeeper.start();
    }

    /**
     * Adds a task to be executed at a later time.
     * WARNING: Do not call this from a Task or a TaskRunner or there will be a deadlock.
     *
     * @param taskBuilder Used to create the task that will be added.
     */
    public void addTask(@NotNull TaskBuilder taskBuilder) {
        synchronized (this) {
            Task task = taskStore.createTask(taskBuilder);
            if (nextTask != null && nextTask.getExecutionTime().isAfter(task.getExecutionTime())) {
                nextTask = task;
            }
            notifyAll();
        }
    }

    private static class TimeKeeper extends Thread {

        private final TaskManager taskManager;

        public TimeKeeper(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (taskManager) {
                    if (taskManager.nextTask == null) {
                        taskManager.nextTask = taskManager.taskStore.getNextTask();
                    }
                    if (taskManager.nextTask != null) {
                        LocalDateTime expiration = taskManager.nextTask.getExecutionTime();
                        LocalDateTime now = LocalDateTime.now();
                        if (expiration.isAfter(now)) {
                            // Wait until task is due or someone adds a task.
                            sleep(expiration, now);
                        } else {
                            performTask();
                        }
                    } else {
                        // Wait until someone adds a task.
                        sleep();
                    }
                }
            }
        }

        private void sleep() {
            try {
                taskManager.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void sleep(@NotNull LocalDateTime expiration, @NotNull LocalDateTime now) {
            long waitDuration = getWaitDuration(expiration, now);
            try {
                if (waitDuration == 0) {
                    taskManager.wait(0, (int) now.until(expiration, ChronoUnit.NANOS));
                } else {
                    taskManager.wait(waitDuration);}
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private long getWaitDuration(@NotNull LocalDateTime expiration, @NotNull LocalDateTime now) {
            long diff = now.until(expiration, ChronoUnit.MILLIS);
            if (diff <= taskManager.precisionCheckTimeMs) {
                return diff;
            } else {
                return diff - taskManager.precisionCheckTimeMs;
            }
        }

        private void performTask() {
            Task task = taskManager.nextTask;
            if (task != null) {
                taskManager.taskRunner.runTask(task);
                if (task instanceof RepeatingTask) {
                    taskManager.taskStore.markRepeated(task);
                } else {
                    taskManager.taskStore.markCompleted(task);
                }
                taskManager.nextTask = null;
            }
        }
    }
}
