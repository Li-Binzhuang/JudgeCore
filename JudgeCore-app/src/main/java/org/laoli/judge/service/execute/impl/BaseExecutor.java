package org.laoli.judge.service.execute.impl;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.service.execute.CodeExecutor;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.service.execute.util.MemoryMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * @Description 执行器抽象基类，提供通用功能
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
public abstract class BaseExecutor implements CodeExecutor {
    
    @Override
    public CaseResult execute(TestCase testCase, Path workDir, String[] command, 
                             long timeLimit, double memoryLimit) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        Process process = null;

        try {
            double memoryUsed = 0;
            // 启动进程并处理输入
            process = pb.start();
            
            // 在单独的线程中写入输入，避免阻塞
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(testCase.input().getBytes(StandardCharsets.UTF_8));
                stdin.flush();
                stdin.close();
            }

            // 异步读取错误流（防止阻塞）
            StringBuilder errorOutput = new StringBuilder();
            Thread errorReader = getErrorReader(process, errorOutput);
            errorReader.start();

            // 启动内存监控线程
            MemoryMonitorThread memoryMonitor = new MemoryMonitorThread(process);
            memoryMonitor.start();

            // 监控执行情况
            long startTime = System.currentTimeMillis();
            boolean completed = process.waitFor(timeLimit, TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis() - startTime;

            // 获取内存使用
            memoryUsed = memoryMonitor.getMaxMemoryUsed();
            memoryMonitor.interrupt();

            // 处理未完成情况
            if (!completed) {
                process.destroyForcibly();
                errorReader.join(1000); // 等待错误流读取完成
                return buildTimeoutResult(testCase, errorOutput.toString(), memoryUsed, executionTime);
            }

            // 等待错误流读取完成
            errorReader.join(1000);

            // 处理已完成情况
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                return buildErrorResult(testCase, errorOutput.toString(), memoryUsed, executionTime);
            }

            // 读取标准输出
            String actualOutput = readInputStream(process.getInputStream());
            return evaluateTestCase(testCase, memoryUsed, executionTime, actualOutput, timeLimit, memoryLimit);

        } catch (Exception e) {
            log.error("执行失败: {}", e.getMessage(), e);
            return buildExceptionResult(testCase, e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 获取错误流读取线程
     */
    protected Thread getErrorReader(Process process, StringBuilder errorOutput) {
        return new Thread(() -> {
            try (InputStream stderr = process.getErrorStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = stderr.read(buffer)) != -1) {
                    errorOutput.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                }
            } catch (IOException ignored) {
                // 流已关闭，忽略
            }
        });
    }

    /**
     * 读取输入流
     */
    protected String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            builder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    /**
     * 构建超时结果
     */
    protected CaseResult buildTimeoutResult(TestCase testCase, String error, double memory, long time) {
        return CaseResult.builder()
                .status(SimpleResult.TIME_LIMIT_EXCEEDED)
                .message(error)
                .input(testCase.input())
                .expectedOutput(testCase.expectedOutput())
                .memoryUsed(memory)
                .executionTime(time)
                .build();
    }

    /**
     * 构建错误结果
     */
    protected CaseResult buildErrorResult(TestCase testCase, String error, double memory, long time) {
        return CaseResult.builder()
                .status(SimpleResult.RUNTIME_ERROR)
                .message(error)
                .input(testCase.input())
                .expectedOutput(testCase.expectedOutput())
                .memoryUsed(memory)
                .executionTime(time)
                .build();
    }

    /**
     * 构建异常结果
     */
    protected CaseResult buildExceptionResult(TestCase testCase, Exception e) {
        return CaseResult.builder()
                .status(SimpleResult.RUNTIME_ERROR)
                .message(e.getMessage())
                .input(testCase.input())
                .expectedOutput(testCase.expectedOutput())
                .build();
    }

    /**
     * 评估测试用例
     */
    protected CaseResult evaluateTestCase(TestCase testCase, double memoryUsed, long executionTime, 
                                         String actualOutput, long timeLimit, double memoryLimit) {
        // 检查内存限制
        if (memoryUsed > memoryLimit) {
            return CaseResult.builder()
                    .status(SimpleResult.MEMORY_LIMIT_EXCEEDED)
                    .executionTime(executionTime)
                    .memoryUsed(memoryUsed)
                    .expectedOutput(testCase.expectedOutput())
                    .actualOutput(actualOutput)
                    .input(testCase.input())
                    .build();
        }

        // 检查时间限制
        if (executionTime > timeLimit) {
            return CaseResult.builder()
                    .status(SimpleResult.TIME_LIMIT_EXCEEDED)
                    .executionTime(executionTime)
                    .actualOutput(actualOutput)
                    .expectedOutput(testCase.expectedOutput())
                    .input(testCase.input())
                    .build();
        }

        // 规范化输出（去除末尾空白字符和多余的换行符）
        String normalizedActual = normalizeOutput(actualOutput);
        String normalizedExpected = normalizeOutput(testCase.expectedOutput());

        // 比较输出
        if (normalizedActual.equals(normalizedExpected)) {
            return CaseResult.builder()
                    .status(SimpleResult.ACCEPTED)
                    .executionTime(executionTime)
                    .memoryUsed(memoryUsed)
                    .build();
        } else {
            return CaseResult.builder()
                    .status(SimpleResult.WRONG_ANSWER)
                    .executionTime(executionTime)
                    .memoryUsed(memoryUsed)
                    .actualOutput(actualOutput)
                    .expectedOutput(testCase.expectedOutput())
                    .input(testCase.input())
                    .build();
        }
    }

    /**
     * 规范化输出
     */
    protected String normalizeOutput(String output) {
        String result = output.trim();
        result = result.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        return result;
    }

    /**
     * 内存监控线程
     */
    private static class MemoryMonitorThread extends Thread {
        private final Process process;
        private double maxMemoryUsed = 0;

        public MemoryMonitorThread(Process process) {
            this.process = process;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (process.isAlive() && !isInterrupted()) {
                    if (process.pid() > 0) {
                        double currentMemory = MemoryMonitor.estimateMemoryUsage(process.pid());
                        maxMemoryUsed = Math.max(maxMemoryUsed, currentMemory);
                    }
                    Thread.sleep(50); // 每50ms采样一次
                }
            } catch (InterruptedException e) {
                // 正常中断
                Thread.currentThread().interrupt();
            }
        }

        public double getMaxMemoryUsed() {
            return maxMemoryUsed;
        }
    }
}
