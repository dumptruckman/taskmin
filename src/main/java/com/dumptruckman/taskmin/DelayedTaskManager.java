package com.dumptruckman.taskmin;

import com.sun.istack.internal.Nullable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DelayedTaskManager implements Runnable {

    private static final long PRECISION_CHECK_TIME_MS = 10000;

    @NotNull
    private final DelayedTaskStore taskStore;
    @NotNull
    private final DelayedTaskRunner taskRunner;

    @Nullable
    private volatile DelayedTask nextTask;

    DelayedTaskManager(@NotNull DelayedTaskStore taskStore, @NotNull DelayedTaskRunner taskRunner) {
        this.taskStore = taskStore;
        this.taskRunner = taskRunner;
        nextTask = taskStore.getNextTask();
    }

    /**
     * Adds a task to be executed at a later time.
     * WARNING: Do not call this from a task or there will be a deadlock.
     *
     * @param expirationTime the time at which the task should be executed.
     * @param task the task to execute.
     */
    public void addTask(@NotNull LocalDateTime expirationTime, @NotNull Runnable task) {
        synchronized (this) {
            DelayedTask delayedTask = taskStore.createTask(expirationTime, task);
            if (nextTask != null && nextTask.getExpirationTime().isAfter(delayedTask.getExpirationTime())) {
                nextTask = delayedTask;
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                if (nextTask == null) {
                    nextTask = taskStore.getNextTask();
                }
                if (nextTask != null) {
                    LocalDateTime expiration = nextTask.getExpirationTime();
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
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sleep(@NotNull LocalDateTime expiration, @NotNull LocalDateTime now) {
        long waitDuration = getWaitDuration(expiration, now);
        try {
            wait(waitDuration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long getWaitDuration(@NotNull LocalDateTime expiration, @NotNull LocalDateTime now) {
        long diff = expiration.until(now, ChronoUnit.MILLIS);
        if (diff <= PRECISION_CHECK_TIME_MS) {
            return diff;
        } else {
            return diff - PRECISION_CHECK_TIME_MS;
        }
    }

    private void performTask() {
        taskRunner.runTask(nextTask);
        taskStore.markCompleted(nextTask);
        nextTask = null;
    }

    public static class DelayedTask implements Comparable<DelayedTask> {

        private final int taskId;
        private final Runnable task;
        private final LocalDateTime expirationTime;

        DelayedTask(int taskId, @NotNull Runnable task, @NotNull LocalDateTime expirationTime) {
            this.taskId = taskId;
            this.task = task;
            this.expirationTime = expirationTime;
        }

        public int getTaskId() {
            return taskId;
        }

        @NotNull
        public Runnable getTask() {
            return task;
        }

        @NotNull
        public LocalDateTime getExpirationTime() {
            return expirationTime;
        }

        @Override
        public int compareTo(@NotNull DelayedTask o) {
            return getExpirationTime().compareTo(o.getExpirationTime());
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (!(o instanceof DelayedTask)) return false;

            final DelayedTask that = (DelayedTask) o;

            return getTaskId() == that.getTaskId();
        }

        @Override
        public int hashCode() {
            return getTaskId();
        }
    }

    public interface DelayedTaskStore {

        /**
         * Retrieves the task that will be the first to expire or null if no tasks remain.
         */
        @Nullable
        DelayedTask getNextTask();

        /**
         * Notifies the backing store that the given task has been completed.
         */
        void markCompleted(@NotNull DelayedTask task);

        /**
         * Adds the given task to the backing store task store and returns the DelayedTask object
         * that represents the task in the store.
         */
        @NotNull
        DelayedTask createTask(@NotNull LocalDateTime expiration, @NotNull Runnable task);
    }

    public interface DelayedTaskRunner {

        /**
         * Runs the given task. This or the task run should never add more tasks or there will be a deadlock.
         */
        void runTask(@NotNull DelayedTask task);
    }
}