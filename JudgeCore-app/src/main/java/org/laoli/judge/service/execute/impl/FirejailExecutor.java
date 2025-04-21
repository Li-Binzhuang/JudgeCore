package org.laoli.judge.service.execute.impl;


import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.service.execute.CodeExecutor;
import org.laoli.judge.model.enums.SimpleResult;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * @Description 沙盒执行器实现
 * @Author laoli
 * @Date 2025/4/20 12:31
 */
@Slf4j
@Component
public class FirejailExecutor implements CodeExecutor {
    //执行器,返回每个测试用例结果
    @Override
    public CaseResult execute(TestCase testCase, Path workDir, String[] command,long timeLimit,double memoryLimit) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());

        pb.redirectErrorStream(true); // 合并标准输出和错误输出
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 启动进程
        Process process = pb.start();

        // 写入输入到进程的标准输入流
        try (OutputStream stdin = process.getOutputStream()) {
            stdin.write(testCase.input().getBytes(StandardCharsets.UTF_8));
            stdin.flush();
        }

        // 估算内存使用（假设 estimateMemoryUsage 是一个已定义的方法）
        double memoryUsed = estimateMemoryUsage(process.pid());

        // 设置超时
        boolean completed = process.waitFor(5, TimeUnit.SECONDS);

        // 记录结束时间
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // 强制终止进程（如果还在运行）
        if (!completed) {
            process.destroyForcibly();
            process.waitFor(1, TimeUnit.SECONDS); // 给进程一点时间来终止
            return CaseResult.builder()
                  .status(SimpleResult.TIME_LIMIT_EXCEEDED)
                  .executionTime(executionTime)
                  .memoryUsed(memoryUsed)
                  .actualOutput("Time limit exceeded")
                  .build();
        }

        // 获取退出代码
        int exitCode = process.exitValue();
        // 读取标准输出
        byte[] buffer = new byte[1024];
        int bytesRead;
        if (exitCode!= 0) {
            StringBuilder errorBuilder = new StringBuilder();
            try(InputStream stderr = process.getErrorStream();){
                // 读取错误输出（如果有）
                while ((bytesRead = stderr.read(buffer)) != -1) {
                    errorBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                }
            }

            return CaseResult.builder()
                 .status(SimpleResult.RUNTIME_ERROR)
                 .actualOutput(errorBuilder.toString())
                 .build();
        }
        // 捕获标准输出和错误输出
        StringBuilder outputBuilder = new StringBuilder();
        try (InputStream stdout = process.getInputStream()) {
            while ((bytesRead = stdout.read(buffer)) != -1) {
                outputBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
        }
        // 返回结果
        String actualOutput = outputBuilder.toString();

        // 检查该测试用例是否通过
        return evaluateTestCase(testCase, memoryUsed, executionTime, actualOutput, timeLimit,memoryLimit);
    }

    private CaseResult evaluateTestCase(TestCase testCase, double memoryUsed, long executionTime, String actualOutput,long timeLimit,double memoryLimit) {
        // 检查内存限制
        if (memoryUsed > memoryLimit) {
            return CaseResult.builder()
                    .status(SimpleResult.MEMORY_LIMIT_EXCEEDED)
                    .executionTime(executionTime)
                    .memoryUsed(memoryUsed)
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
                   .input(testCase.input())
                   .build();
        }

        // 比较输出
        if (compareOutput(actualOutput, testCase.expectedOutput())) {
            // 输出比较通过
            return CaseResult.builder()
                    .status(SimpleResult.ACCEPTED)
                    .executionTime(executionTime)
                    .memoryUsed(memoryUsed)
                    .build();
        } else {
            // 输出比较失败
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

    private boolean compareOutput(String actual, String expected) {
        // 规范化输出（去除末尾空白字符和多余的换行符）
        String normalizedActual = normalizeOutput(actual);
        String normalizedExpected = normalizeOutput(expected);
        return normalizedActual.equals(normalizedExpected);
    }

    private String normalizeOutput(String output) {
        // 移除末尾空白字符
        String result = output.trim();
        // 规范化不同操作系统的换行符
        result = result.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        return result;
    }

    // 估算内存使用的方法
    private double estimateMemoryUsage(Long pid) {
        long memoryUsageKiB=0;
        try {
            ProcessBuilder pbQuery = new ProcessBuilder("ps", "-o", "rss=", "-p", String.valueOf(pid));
            pbQuery.redirectError(ProcessBuilder.Redirect.INHERIT); // 将错误流重定向到当前进程的错误流
            Process queryProcess = pbQuery.start();
            // 3. 读取命令输出
            String memoryUsageStr = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(queryProcess.getInputStream()))) {
                memoryUsageStr = reader.readLine();
            }
            int exitCode = queryProcess.waitFor();
            if(exitCode==0&&memoryUsageStr != null && !memoryUsageStr.trim().isEmpty()){
                memoryUsageKiB = Long.parseLong(memoryUsageStr.trim());
                return memoryUsageKiB;
            }else {
                return 1;
            }
        } catch (IOException | InterruptedException e) {
            log.info(e.getMessage());
            return 1;
        }
    }
}
