package org.laoli.judge.service.execute.impl;


import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.service.execute.CodeExecutor;
import org.laoli.judge.model.enums.SimpleResult;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description 沙盒执行器实现
 * @Author laoli
 * @Date 2025/4/20 12:31
 */
@Slf4j
@Component
public class FirejailExecutor implements CodeExecutor {
    @Override
    public CaseResult execute(TestCase testCase, Path workDir, String[] command, long timeLimit, double memoryLimit) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        Process process = null;

        try {
            double memoryUsed = 0 ;
            // 启动进程并处理输入
            process = pb.start();
            try (OutputStream stdin = process.getOutputStream())  {
                stdin.write(testCase.input().getBytes(StandardCharsets.UTF_8));
                stdin.flush();
                memoryUsed = estimateMemoryUsage(process.pid());
            }

            // 异步读取错误流（防止阻塞）
            StringBuilder errorOutput = new StringBuilder();
            Thread errorReader = getErrorReader(process, errorOutput);
            errorReader.start();

            // 监控执行情况
            long startTime = System.currentTimeMillis();
            boolean completed = process.waitFor(timeLimit,  TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis()  - startTime;


            // 处理未完成情况
            if (!completed) {
                process.destroyForcibly();
                return buildTimeoutResult(testCase, errorOutput.toString(),  memoryUsed, executionTime);
            }

            // 处理已完成情况
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                return buildErrorResult(testCase, errorOutput.toString(),  memoryUsed, executionTime);
            }

            // 读取标准输出
            String actualOutput = readInputStream(process.getInputStream());
            return evaluateTestCase(testCase, memoryUsed, executionTime, actualOutput, timeLimit, memoryLimit);

        } catch (Exception e) {
            log.error(" 执行失败: {}", e.getMessage());
            return buildExceptionResult(testCase, e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static Thread getErrorReader(Process process, StringBuilder errorOutput) {
        return new Thread(() -> {
            try (InputStream stderr = process.getErrorStream())  {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = stderr.read(buffer))  != -1) {
                    errorOutput.append(new  String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                }
            } catch (IOException ignored) {}
        });
    }

    // 辅助方法 - 读取输入流
    private String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer))  != -1) {
            builder.append(new  String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    // 辅助方法 - 构建超时结果
    private CaseResult buildTimeoutResult(TestCase testCase, String error, double memory, long time) {
        return CaseResult.builder()
                .status(SimpleResult.TIME_LIMIT_EXCEEDED)
                .message(error)
                .input(testCase.input())
                .expectedOutput(testCase.expectedOutput())
                .memoryUsed(memory)
                .executionTime(time)
                .build();
    }

    // 辅助方法 - 构建错误结果
    private CaseResult buildErrorResult(TestCase testCase, String error, double memory, long time) {
        return CaseResult.builder()
                .status(SimpleResult.RUNTIME_ERROR)
                .message(error)
                .input(testCase.input())
                .expectedOutput(testCase.expectedOutput())
                .memoryUsed(memory)
                .executionTime(time)
                .build();
    }

    // 辅助方法 - 构建异常结果
    private CaseResult buildExceptionResult(TestCase testCase, Exception e) {
        return CaseResult.builder()
                .status(SimpleResult.RUNTIME_ERROR)
                .message(e.getMessage())
                .input(testCase.input())
                .expectedOutput(testCase.expectedOutput())
                .build();
    }
    //执行器,返回每个测试用例结果
//    @Override
//    public CaseResult execute(TestCase testCase, Path workDir, String[] command,long timeLimit,double memoryLimit) {
//        ProcessBuilder pb = new ProcessBuilder(command);
//        pb.directory(workDir.toFile());
//        long startTime=0;
//        double memoryUsed=0;
//        boolean completed=false;
//        long executionTime=0;
//        OutputStream stdin = null;
//        Process process = null;
//        //执行代码逻辑
//        try{
//            process = pb.start();
//            stdin = process.getOutputStream();
//            stdin.write(testCase.input().getBytes(StandardCharsets.UTF_8));
//            // 估算内存使用（假设 estimateMemoryUsage 是一个已定义的方法）
//            memoryUsed = estimateMemoryUsage(process.pid());
//            // 记录开始时间
//            startTime = System.currentTimeMillis();
//            stdin.flush();
//            // 设置超时
//            completed = process.waitFor(5, TimeUnit.SECONDS);
//            log.info("执行完成:{}",completed);
//            // 记录结束时间
//            executionTime = System.currentTimeMillis() - startTime;
//            process.destroy();
//
//        }catch (Exception e) {
//            log.error("执行失败:{}",e.getMessage());
//            return CaseResult.builder()
//                    .expectedOutput(testCase.expectedOutput())
//                    .input(testCase.input())
//                    .status(SimpleResult.RUNTIME_ERROR)
//                    .message(e.getMessage())
//                    .build();
//        }
//        // 读取标准输出
//        byte[] buffer = new byte[1024];
//        int bytesRead;
//        // 获取退出代码
//        int exitCode = process.exitValue();
//        StringBuilder errorBuilder = new StringBuilder();
//        if(!completed||exitCode!=0){
//            try(InputStream stderr = process.getErrorStream();) {
//                // 读取错误输出（如果有）
//                while ((bytesRead = stderr.read(buffer)) != -1) {
//                    errorBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
//                }
//                if (!completed) {
//                    return CaseResult.builder()
//                            .status(SimpleResult.TIME_LIMIT_EXCEEDED)
//                            .message(errorBuilder.toString())
//                            .input(testCase.input())
//                            .expectedOutput(testCase.expectedOutput())
//                            .build();
//                }else {
//                    return CaseResult.builder()
//                            .message(errorBuilder.toString())
//                            .status(SimpleResult.RUNTIME_ERROR)
//                            .build();
//                }
//            }catch (Exception e){
//                log.error("读取错误输出失败:{}",e.getMessage());
//                return CaseResult.builder()
//                      .status(SimpleResult.RUNTIME_ERROR)
//                      .message(e.getMessage())
//                      .build();
//            }
//        }
//
//        // 获取返回结果
//        StringBuilder outputBuilder = new StringBuilder();
//        InputStream stdout = null;
//        try {
//            stdout = process.getInputStream();
//            while ((bytesRead = stdout.read(buffer)) != -1) {
//                outputBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
//            }
//            stdout.close();
//        } catch (Exception e) {
//            log.error("读取标准输出失败:{}",e.getMessage());
//            return CaseResult.builder()
//                    .status(SimpleResult.WRONG_ANSWER)
//                    .expectedOutput(testCase.expectedOutput())
//                    .actualOutput(outputBuilder.toString())
//                    .input(testCase.input())
//                    .message(e.getMessage())
//                    .build();
//        }
//        // 返回结果
//        String actualOutput = outputBuilder.toString();
//
//        // 检查该测试用例是否通过
//        return evaluateTestCase(testCase, memoryUsed, executionTime, actualOutput, timeLimit,memoryLimit);
//    }

    private CaseResult evaluateTestCase(TestCase testCase, double memoryUsed,long executionTime, String actualOutput,long timeLimit,double memoryLimit) {
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

    private String normalizeOutput(String output) {
        // 移除末尾空白字符
        String result = output.trim();
        // 规范化不同操作系统的换行符
        result = result.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        return result;
    }

    private double estimateMemoryUsage(Long pid) {
        long residentPages = 0;
        try {
            Path statmPath = Paths.get("/proc", String.valueOf(pid), "statm");
            for(int i=0;i<20;i++){
                List<String> lines = Files.readAllLines(statmPath);
                if (lines.isEmpty()) {
                    return residentPages << 2;
                }
                String[] stats = lines.get(0).split("\\s+");
                residentPages = Math.max(Long.parseLong(stats[1]),residentPages); // 第二列为常驻集大小（单位：页）
            }
            return residentPages << 2;
        } catch (IOException e) {
            log.info("Error reading memory usage: " + e.getMessage());
            return residentPages << 2;
        }
    }
}
