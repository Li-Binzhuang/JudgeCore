package org.laoli.judge.service.monitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.laoli.judge.service.monitor.PerformanceMonitor.PerformanceMetrics;
import org.laoli.judge.service.monitor.PerformanceMonitor.PerformanceSummary;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PerformanceMonitor Tests")
class PerformanceMonitorTest {

    private PerformanceMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor = new PerformanceMonitor();
    }

    @Nested
    @DisplayName("Recording Execution Metrics")
    class RecordingMetrics {

        @Test
        @DisplayName("Should record execution time")
        void shouldRecordExecutionTime() {
            monitor.recordExecution("test1", 100L, 1024L, true);
            PerformanceMetrics metrics = monitor.getMetrics("test1");
            assertEquals(100L, metrics.totalExecutionTime());
        }

        @Test
        @DisplayName("Should accumulate execution time for same test case")
        void shouldAccumulateExecutionTime() {
            monitor.recordExecution("test1", 100L, 1024L, true);
            monitor.recordExecution("test1", 150L, 2048L, true);
            PerformanceMetrics metrics = monitor.getMetrics("test1");
            assertEquals(250L, metrics.totalExecutionTime());
        }

        @Test
        @DisplayName("Should track maximum memory usage")
        void shouldTrackMaximumMemoryUsage() {
            monitor.recordExecution("test1", 100L, 1024L, true);
            monitor.recordExecution("test1", 150L, 2048L, true);
            PerformanceMetrics metrics = monitor.getMetrics("test1");
            assertEquals(2048L, metrics.maxMemoryUsed());
        }

        @Test
        @DisplayName("Should track success count")
        void shouldTrackSuccessCount() {
            monitor.recordExecution("test1", 100L, 1024L, true);
            monitor.recordExecution("test1", 150L, 2048L, true);
            monitor.recordExecution("test1", 200L, 3072L, false);
            PerformanceMetrics metrics = monitor.getMetrics("test1");
            assertEquals(2L, metrics.successCount());
            assertEquals(1L, metrics.failureCount());
        }

        @Test
        @DisplayName("Should track last execution time")
        void shouldTrackLastExecutionTime() {
            monitor.recordExecution("test1", 100L, 1024L, true);
            monitor.recordExecution("test1", 150L, 2048L, true);
            PerformanceMetrics metrics = monitor.getMetrics("test1");
            assertEquals(150L, metrics.lastExecutionTime());
        }
    }

    @Nested
    @DisplayName("Summary Generation")
    class SummaryGeneration {

        @Test
        @DisplayName("Should generate correct summary")
        void shouldGenerateCorrectSummary() {
            monitor.recordExecution("test1", 100L, 1024L, true);
            monitor.recordExecution("test2", 200L, 2048L, true);
            monitor.recordExecution("test3", 300L, 3072L, false);

            PerformanceSummary summary = monitor.getSummary();

            assertEquals(600L, summary.totalExecutionTime());
            assertEquals(3072L, summary.maxMemoryUsed());
            assertEquals(2L, summary.totalSuccessCount());
            assertEquals(1L, summary.totalFailureCount());
        }

        @Test
        @DisplayName("Should handle empty monitor")
        void shouldHandleEmptyMonitor() {
            PerformanceSummary summary = monitor.getSummary();

            assertEquals(0L, summary.totalExecutionTime());
            assertEquals(0L, summary.maxMemoryUsed());
            assertEquals(0L, summary.totalSuccessCount());
            assertEquals(0L, summary.totalFailureCount());
        }
    }

    @Nested
    @DisplayName("Reset Functionality")
    class ResetFunctionality {

        @Test
        @DisplayName("Should clear all metrics on reset")
        void shouldClearAllMetricsOnReset() {
            monitor.recordExecution("test1", 100L, 1024L, true);
            monitor.recordExecution("test2", 200L, 2048L, false);

            monitor.reset();

            PerformanceSummary summary = monitor.getSummary();
            assertEquals(0L, summary.totalExecutionTime());
            assertEquals(0L, summary.maxMemoryUsed());

            PerformanceMetrics metrics1 = monitor.getMetrics("test1");
            assertEquals(0L, metrics1.totalExecutionTime());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle multiple test cases with different patterns")
        void shouldHandleMultipleTestCases() {
            monitor.recordExecution("case_a", 50L, 1000L, true);
            monitor.recordExecution("case_b", 100L, 2000L, true);
            monitor.recordExecution("case_a", 75L, 1500L, true);

            PerformanceMetrics metricsA = monitor.getMetrics("case_a");
            PerformanceMetrics metricsB = monitor.getMetrics("case_b");

            assertEquals(125L, metricsA.totalExecutionTime());
            assertEquals(1500L, metricsA.maxMemoryUsed());
            assertEquals(2L, metricsA.successCount());

            assertEquals(100L, metricsB.totalExecutionTime());
            assertEquals(2000L, metricsB.maxMemoryUsed());
            assertEquals(1L, metricsB.successCount());
        }

        @Test
        @DisplayName("Should return zeros for non-existent test case")
        void shouldReturnZerosForNonExistentTestCase() {
            PerformanceMetrics metrics = monitor.getMetrics("nonexistent");

            assertEquals(0L, metrics.totalExecutionTime());
            assertEquals(0L, metrics.maxMemoryUsed());
            assertEquals(0L, metrics.successCount());
            assertEquals(0L, metrics.failureCount());
            assertEquals(0L, metrics.lastExecutionTime());
        }
    }
}
