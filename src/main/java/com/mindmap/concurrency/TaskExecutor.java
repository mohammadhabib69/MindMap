package com.mindmap.concurrency;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized background task execution infrastructure for MindMap.
 * Manages a small, controlled thread pool of daemon worker threads to execute
 * database queries and heavy computations off the JavaFX Application Thread.
 */
public final class TaskExecutor {

    private static final Logger LOGGER = Logger.getLogger(TaskExecutor.class.getName());
    private static final int POOL_SIZE = 3;

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    private static volatile ExecutorService executor = createExecutor();

    private TaskExecutor() {
        // Prevent instantiation
    }

    private static ExecutorService createExecutor() {
        ThreadFactory daemonFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("mindmap-worker-" + THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true); // Daemon threads ensure JVM exits cleanly without hanging
            thread.setUncaughtExceptionHandler((t, e) ->
                    LOGGER.log(Level.SEVERE, "Uncaught exception in worker thread " + t.getName(), e));
            return thread;
        };
        return Executors.newFixedThreadPool(POOL_SIZE, daemonFactory);
    }

    /**
     * Executes a runnable task on a background worker thread.
     *
     * @param runnable The runnable to execute.
     */
    public static void execute(Runnable runnable) {
        if (runnable == null) return;
        ensureActiveExecutor();
        executor.execute(runnable);
    }

    /**
     * Submits a callable to the background worker pool and returns a Future.
     *
     * @param callable The task to submit.
     * @param <T> Return type of the callable.
     * @return Future representing the pending result.
     */
    public static <T> Future<T> submit(Callable<T> callable) {
        if (callable == null) {
            throw new IllegalArgumentException("Callable cannot be null");
        }
        ensureActiveExecutor();
        return executor.submit(callable);
    }

    /**
     * Submits a JavaFX Task to the background executor for execution.
     *
     * @param task The JavaFX Task to execute.
     * @param <T> The task's return type.
     */
    public static <T> void executeTask(Task<T> task) {
        if (task == null) return;
        ensureActiveExecutor();
        executor.execute(task);
    }

    /**
     * Runs a supplier task in the background and safely dispatches the result to the
     * onSuccess consumer on the JavaFX Application Thread via Platform.runLater().
     * If an error occurs, dispatches the throwable to the onError consumer on the JavaFX thread.
     *
     * @param backgroundSupplier Computes data on worker thread (e.g. database query).
     * @param uiSuccessHandler Dispatched on JavaFX Application Thread with the result.
     * @param uiErrorHandler Dispatched on JavaFX Application Thread if an error occurs.
     * @param <T> Result type.
     */
    public static <T> void runAsync(Supplier<T> backgroundSupplier,
                                    Consumer<T> uiSuccessHandler,
                                    Consumer<Throwable> uiErrorHandler) {
        if (backgroundSupplier == null) return;

        execute(() -> {
            try {
                T result = backgroundSupplier.get();
                if (uiSuccessHandler != null) {
                    Platform.runLater(() -> uiSuccessHandler.accept(result));
                }
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Background execution error: " + t.getMessage(), t);
                if (uiErrorHandler != null) {
                    Platform.runLater(() -> uiErrorHandler.accept(t));
                }
            }
        });
    }

    /**
     * Convenience method to run a background action with a UI completion callback.
     *
     * @param backgroundAction Action to run on worker thread (e.g. database write).
     * @param uiCompletionHandler Callback executed on JavaFX Application Thread after completion.
     */
    public static void runAsyncAction(Runnable backgroundAction, Runnable uiCompletionHandler) {
        if (backgroundAction == null) return;

        execute(() -> {
            try {
                backgroundAction.run();
                if (uiCompletionHandler != null) {
                    Platform.runLater(uiCompletionHandler);
                }
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "Background action error: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Gracefully shuts down the background thread pool.
     */
    public static synchronized void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            LOGGER.info("Shutting down MindMap background task executor...");
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOGGER.info("MindMap background task executor terminated.");
        }
    }

    /**
     * Checks if the executor has been shut down.
     */
    public static boolean isShutdown() {
        return executor == null || executor.isShutdown();
    }

    /**
     * Reinitializes the executor if it was shut down (primarily used in test suites).
     */
    public static synchronized void ensureActiveExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = createExecutor();
        }
    }
}
