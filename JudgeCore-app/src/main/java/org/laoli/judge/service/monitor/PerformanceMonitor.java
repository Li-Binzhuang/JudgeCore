package org.laoli.judge.service.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class PerformanceMonitor {

    private final ConcurrentHashMap<String, AtomicLong> executionTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> memoryUsages = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> successCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> failureCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicReference<Long>> lastExecutionTime = new ConcurrentHashMap<>();

    public void recordExecution(String testCaseId, long executionTime, long memoryUsed, boolean success) {
        executionTimes.computeIfAbsent(testCaseId, k -> new AtomicLong(0)).addAndGet(executionTime);
        memoryUsages.computeIfAbsent(testCaseId, k -> new AtomicLong(0)).updateAndGet(v -> Math.max(v, memoryUsed));
        
        if (success) {
            successCounts.computeIfAbsent(testCaseId, k -> new AtomicLong(0)).incrementAndGet();
        } else {
            failureCounts.computeIfAbsent(testCaseId, k -> new AtomicLong(0)).incrementAndGet();
        }
        
        lastExecutionTime.computeIfAbsent(testCaseId, k -> new AtomicReference<>(0L)).set(executionTime);
    }

    public PerformanceMetrics getMetrics(String testCaseId) {
        AtomicLong totalTime = executionTimes.get(testCaseId);
        AtomicLong maxMemory = memoryUsages.get(testCaseId);
        AtomicLong success = successCounts.get(testCaseId);
        AtomicLong failure = failureCounts.get(testCaseId);
        AtomicReference<Long> lastTime = lastExecutionTime.get(testCaseId);

        return new PerformanceMetrics(
                totalTime != null ? totalTime.get() : 0,
                maxMemory != null ? maxMemory.get() : 0,
                success != null ? success.get() : 0,
                failure != null ? failure.get() : 0,
                lastTime != null ? lastTime.get() : 0
        );
    }

    public PerformanceSummary getSummary() {
        long totalExecTime = executionTimes.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
        
        long maxMemory = memoryUsages.values().stream()
                .mapToLong(AtomicLong::get)
                .max()
                .orElse(0);
        
        long totalSuccess = successCounts.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
        
        long totalFailure = failureCounts.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();

        return new PerformanceSummary(totalExecTime, maxMemory, totalSuccess, totalFailure);
    }

    public void reset() {
        executionTimes.clear();
        memoryUsages.clear();
        successCounts.clear();
        failureCounts.clear();
        lastExecutionTime.clear();
    }

    public record PerformanceMetrics(
            long totalExecutionTime,
            long maxMemoryUsed,
            long successCount,
            long failureCount,
            long lastExecutionTime
    ) {
    }

    public record PerformanceSummary(
            long totalExecutionTime,
            long maxMemoryUsed,
            long totalSuccessCount,
            long totalFailureCount
    ) {
    }
}
