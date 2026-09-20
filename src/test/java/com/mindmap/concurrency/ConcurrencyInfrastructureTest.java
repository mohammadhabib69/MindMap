package com.mindmap.concurrency;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying TaskExecutor thread naming, daemon status,
 * async dispatch, and JavaFX Application Thread safety.
 */
public class ConcurrencyInfrastructureTest {

    private static boolean toolkitInitialized = false;

    @BeforeAll
    static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            toolkitInitialized = true;
            latch.await(3, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            // Already initialized
            toolkitInitialized = true;
        }
    }

    @Test
    void testWorkerThreadNamingAndDaemonStatus() throws Exception {
        TaskExecutor.ensureActiveExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();
        AtomicBoolean isDaemon = new AtomicBoolean();

        TaskExecutor.execute(() -> {
            Thread current = Thread.currentThread();
            threadName.set(current.getName());
            isDaemon.set(current.isDaemon());
            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Worker task did not execute in time");
        assertNotNull(threadName.get());
        assertTrue(threadName.get().startsWith("mindmap-worker-"), "Thread should be named mindmap-worker-*: " + threadName.get());
        assertTrue(isDaemon.get(), "Worker thread must be a daemon thread to allow clean JVM exit");
    }

    @Test
    void testSubmitCallable() throws Exception {
        TaskExecutor.ensureActiveExecutor();
        Callable<Integer> task = () -> 42 * 2;
        Future<Integer> future = TaskExecutor.submit(task);
        assertNotNull(future);
        Integer result = future.get(3, TimeUnit.SECONDS);
        assertEquals(84, result);
    }

    @Test
    void testRunAsyncDispatchesToFxThread() throws Exception {
        if (!toolkitInitialized) return;
        TaskExecutor.ensureActiveExecutor();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> backgroundThreadName = new AtomicReference<>();
        AtomicBoolean ranOnFxThread = new AtomicBoolean(false);
        AtomicReference<String> receivedResult = new AtomicReference<>();

        TaskExecutor.runAsync(
                () -> {
                    backgroundThreadName.set(Thread.currentThread().getName());
                    return "ComputedAsyncData";
                },
                result -> {
                    ranOnFxThread.set(Platform.isFxApplicationThread());
                    receivedResult.set(result);
                    latch.countDown();
                },
                error -> {
                    latch.countDown();
                }
        );

        assertTrue(latch.await(4, TimeUnit.SECONDS), "Async task timed out");
        assertNotNull(backgroundThreadName.get());
        assertTrue(backgroundThreadName.get().startsWith("mindmap-worker-"), "Background work must run on worker thread");
        assertTrue(ranOnFxThread.get(), "Success handler must run on JavaFX Application Thread");
        assertEquals("ComputedAsyncData", receivedResult.get());
    }

    @Test
    void testRunAsyncErrorHandlerDispatchesToFxThread() throws Exception {
        if (!toolkitInitialized) return;
        TaskExecutor.ensureActiveExecutor();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean errorRanOnFx = new AtomicBoolean(false);
        AtomicReference<Throwable> capturedError = new AtomicReference<>();

        TaskExecutor.runAsync(
                () -> {
                    throw new RuntimeException("Simulated background error");
                },
                result -> {
                    latch.countDown();
                },
                throwable -> {
                    errorRanOnFx.set(Platform.isFxApplicationThread());
                    capturedError.set(throwable);
                    latch.countDown();
                }
        );

        assertTrue(latch.await(4, TimeUnit.SECONDS), "Error task timed out");
        assertTrue(errorRanOnFx.get(), "Error handler must run on JavaFX Application Thread");
        assertNotNull(capturedError.get());
        assertEquals("Simulated background error", capturedError.get().getMessage());
    }

    @Test
    void testRunAsyncAction() throws Exception {
        if (!toolkitInitialized) return;
        TaskExecutor.ensureActiveExecutor();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean actionRan = new AtomicBoolean(false);
        AtomicBoolean completionRanOnFx = new AtomicBoolean(false);

        TaskExecutor.runAsyncAction(
                () -> actionRan.set(true),
                () -> {
                    completionRanOnFx.set(Platform.isFxApplicationThread());
                    latch.countDown();
                }
        );

        assertTrue(latch.await(4, TimeUnit.SECONDS), "Async action timed out");
        assertTrue(actionRan.get(), "Background action must have run");
        assertTrue(completionRanOnFx.get(), "Completion callback must run on FX Application Thread");
    }

    @Test
    void testMainStopGracefulShutdown() throws Exception {
        TaskExecutor.ensureActiveExecutor();
        assertFalse(TaskExecutor.isShutdown());

        // Simulate Main.stop()
        new com.mindmap.Main().stop();
        assertTrue(TaskExecutor.isShutdown(), "TaskExecutor should be shut down after Main.stop()");

        // Verify ensureActiveExecutor revives the pool cleanly for subsequent operations
        TaskExecutor.ensureActiveExecutor();
        assertFalse(TaskExecutor.isShutdown(), "TaskExecutor should be active after ensureActiveExecutor()");
    }
}
