package org.laoli.judge.service.execute.impl;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.service.comparator.ComparatorFactory;
import org.laoli.judge.service.comparator.OutputComparator;
import org.laoli.judge.service.execute.CodeExecutor;
import org.laoli.judge.util.ProcessUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FirejailExecutor implements CodeExecutor {

    private final OutputComparator comparator = ComparatorFactory.getComparator("exact");

    @Override
    public CaseResult execute(TestCase testCase, Path workDir, String[] command, long timeLimit, long memoryLimit) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        Process process = null;

        try {
            process = pb.start();
            writeInput(process, testCase.input());

            long memoryUsed = ProcessUtils.estimateMemoryUsage(process.pid());

            Thread errorReader = startErrorReader(process);
            errorReader.start();

            long startTime = System.currentTimeMillis();
            boolean completed = process.waitFor(timeLimit, TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis() - startTime;

            if (!completed) {
                process.destroyForcibly();
                return buildResult(SimpleResult.TIME_LIMIT_EXCEEDED, null, memoryUsed, executionTime, testCase);
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String errorOutput = getErrorOutput(errorReader);
                return buildResult(SimpleResult.RUNTIME_ERROR, errorOutput, memoryUsed, executionTime, testCase);
            }

            String actualOutput = ProcessUtils.readInputStream(process.getInputStream());
            return evaluateResult(testCase, memoryUsed, executionTime, actualOutput, timeLimit, memoryLimit);

        } catch (Exception e) {
            log.error("执行失败: {}", e.getMessage());
            return buildResult(SimpleResult.RUNTIME_ERROR, e.getMessage(), 0, 0, testCase);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private void writeInput(Process process, String input) throws IOException {
        try (OutputStream stdin = process.getOutputStream()) {
            stdin.write(input.getBytes(StandardCharsets.UTF_8));
            stdin.flush();
        }
    }

    private Thread startErrorReader(Process process) {
        StringBuilder errorOutput = new StringBuilder();
        return new Thread(() -> {
            try (InputStream stderr = process.getErrorStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = stderr.read(buffer)) != -1) {
                    errorOutput.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                }
            } catch (IOException ignored) {
            }
        });
    }

    private String getErrorOutput(Thread errorReader) throws InterruptedException {
        errorReader.join(100);
        return "";
    }

    private CaseResult buildResult(SimpleResult status, String message, long memory, long time, TestCase testCase) {
        return CaseResult.builder()
                .status(status)
                .message(message)
                .memoryUsed(memory)
                .executionTime(time)
                .input(testCase.input())
                .expectedOutput(testCase.expectedOutput())
                .build();
    }

    private CaseResult evaluateResult(TestCase testCase, long memoryUsed, long executionTime, String actualOutput,
            long timeLimit, long memoryLimit) {
        if (memoryUsed > memoryLimit) {
            return CaseResult.builder()
                    .status(SimpleResult.MEMORY_LIMIT_EXCEEDED)
                    .executionTime(executionTime)
                    .memoryUsed(memoryUsed)
                    .expectedOutput(testCase.expectedOutput())
                    .actualOutput(ProcessUtils.normalizeOutput(actualOutput))
                    .input(testCase.input())
                    .build();
        }

        if (executionTime > timeLimit) {
            return CaseResult.builder()
                    .status(SimpleResult.TIME_LIMIT_EXCEEDED)
                    .executionTime(executionTime)
                    .actualOutput(ProcessUtils.normalizeOutput(actualOutput))
                    .expectedOutput(testCase.expectedOutput())
                    .input(testCase.input())
                    .build();
        }

        CaseResult result = CaseResult.builder()
                .executionTime(executionTime)
                .memoryUsed(memoryUsed)
                .actualOutput(actualOutput)
                .expectedOutput(testCase.expectedOutput())
                .input(testCase.input())
                .build();

        return comparator.compare(result);
    }
}
