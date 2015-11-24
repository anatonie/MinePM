package de.maxikg.minepm.reporter;

import de.maxikg.minepm.reporter.adapter.ReportingAdapter;
import org.aspectj.lang.Signature;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Reporter {

    private static volatile boolean initialized = false;
    private static ExecutorService worker;
    private static ReportingAdapter adapter;

    private Reporter() {
    }

    public static synchronized void init(ReportingAdapter adapter) {
        if (initialized)
            throw new IllegalStateException("Already initialized.");

        try {
            adapter.init();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        Reporter.initialized = true;
        Reporter.worker = Executors.newSingleThreadExecutor();
        Reporter.adapter = Objects.requireNonNull(adapter, "adapter must be not null.");
    }

    public static void reportEventExecution(String eventClass, Signature signature, long millis, boolean async) {
        checkInitialized();
        long date = System.currentTimeMillis();
        worker.submit((Runnable) () -> adapter.saveEventExecutionReport(date, eventClass, signature, millis, async));
    }

    public static void reportChunkLoad(Object world, int x, int z, long millis) {
        checkInitialized();
        long date = System.currentTimeMillis();
        worker.submit((Runnable) () -> adapter.saveChunkLoadReport(date, world, x, z, millis));
    }

    public static void shutdown() {
        worker.shutdown();
        worker = null;
        try {
            adapter.shutdown();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        adapter = null;
        initialized = false;
    }

    private static void checkInitialized() {
        if (!initialized)
            throw new IllegalStateException("Not initialized.");
    }
}
