package org.laoli;
/*
 *@description TODO
 *@author laoli
 *@create 2025/4/19 17:27
 */

/**
 * Online Judge System - Core Implementation
 */

// 导入必要的包

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 测试用例数据类
 */
@Getter
@AllArgsConstructor
class TestCase {
    private final String input;
    private final String expectedOutput;
    private final long timeLimit;  // 毫秒
    private final int memoryLimit; // MB
}

/**
 * 判题结果状态枚举
 */
@Getter
@AllArgsConstructor
enum SimpleResult {
    ACCEPTED("Accepted"),
    WRONG_ANSWER("Wrong Answer"),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded"),
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded"),
    RUNTIME_ERROR("Runtime Error"),
    COMPILATION_ERROR("Compilation Error"),
    SYSTEM_ERROR("System Error");

    private final String description;
}

/**
 * 单个测试用例的执行结果
 */
@Getter
@AllArgsConstructor
class CaseResult {
    private final SimpleResult status;
    private final String message;
    private final long executionTime;
    private final double memoryUsed;
    private final String actualOutput;
}

/**
 * 所有测试用例的汇总结果
 */
@Getter
@AllArgsConstructor
class JudgeResult {
    private final SimpleResult status;
    private final String message;
    private final long executionTime;
    private final double memoryUsed;
    private final List<CaseResult> caseResults;
}

/**
 * 语言支持枚举
 */
@AllArgsConstructor
@Getter
enum Language {
    JAVA("java"),
    PYTHON("python"),
    CPP("cpp"),
    C("c"),
    GO("go"),
    JAVASCRIPT("javascript"),
    RUBY("ruby"),
    PHP("php"),
    RUST("rust"),
    SWIFT("swift"),
    KOTLIN("kotlin");
    private final String Language;
}

/**
 * 代码编译器接口
 */
interface Compiler {
    boolean compile(String sourceCode, Path workDir) throws IOException, InterruptedException;
    String getCompilationErrorMessage();
}

/**
 * Java语言编译器实现
 */
class JavaCompiler implements Compiler {
    private String compilationErrorMessage;

    @Override
    public boolean compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("Main.java");
        Files.write(sourceFile, sourceCode.getBytes());

        // 编译Java代码
        ProcessBuilder pb = new ProcessBuilder("javac", sourceFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 获取编译输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            compilationErrorMessage = output.toString();
            return false;
        }
        return true;
    }

    @Override
    public String getCompilationErrorMessage() {
        return compilationErrorMessage;
    }
}

/**
 * Python语言编译器实现
 */
class PythonCompiler implements Compiler {
    private String compilationErrorMessage;

    @Override
    public boolean compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        try {
            // 创建源代码文件
            Path sourceFile = workDir.resolve("solution.py");
            Files.write(sourceFile, sourceCode.getBytes());

            // 检查Python语法
            ProcessBuilder pb = new ProcessBuilder("python3", "-m", "py_compile", sourceFile.toString());
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 获取编译输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                compilationErrorMessage = output.toString();
                return false;
            }
            return true;
        } catch (Exception e) {
            compilationErrorMessage = e.getMessage();
            return false;
        }
    }

    @Override
    public String getCompilationErrorMessage() {
        return compilationErrorMessage;
    }
}

/**
 * C++语言编译器实现
 */
class CppCompiler implements Compiler {
    private String compilationErrorMessage;

    @Override
    public boolean compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("solution.cpp");
        Files.write(sourceFile, sourceCode.getBytes());

        // 编译C++代码
        ProcessBuilder pb = new ProcessBuilder("g++", "-o", "solution", sourceFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 获取编译输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            compilationErrorMessage = output.toString();
            return false;
        }
        return true;
    }

    @Override
    public String getCompilationErrorMessage() {
        return compilationErrorMessage;
    }
}

/**
 * C语言编译器实现
 */
class CCompiler implements Compiler {
    private String compilationErrorMessage;

    @Override
    public boolean compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("solution.c");
        Files.write(sourceFile, sourceCode.getBytes());

        // 编译C代码
        ProcessBuilder pb = new ProcessBuilder("gcc", "-o", "solution", sourceFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 获取编译输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            compilationErrorMessage = output.toString();
            return false;
        }
        return true;
    }

    @Override
    public String getCompilationErrorMessage() {
        return compilationErrorMessage;
    }
}

/**
 * Go语言编译器实现
 */
class GoCompiler implements Compiler {
    private String compilationErrorMessage;

    @Override
    public boolean compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("solution.go");
        Files.write(sourceFile, sourceCode.getBytes());
        // 编译Go代码
        ProcessBuilder pb = new ProcessBuilder("go", "build", "-o", "solution", sourceFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        // 获取编译输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine())!= null) {
            output.append(line).append("\n");
        }
        int exitCode = process.waitFor();
        if (exitCode!= 0) {
            compilationErrorMessage = output.toString();
        }
        return exitCode== 0;
    }

    @Override
    public String getCompilationErrorMessage() {
        return compilationErrorMessage;
    }
}



//PHP语言编译器实现
class PHPCompiler implements Compiler {
    private String compilationErrorMessage;
    @Override
    public boolean compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("solution.php");
        Files.write(sourceFile, sourceCode.getBytes());
        // 编译PHP代码
        ProcessBuilder pb = new ProcessBuilder("php", "-l", sourceFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        // 获取编译输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine())!= null) {
            output.append(line).append("\n");
        }
        int exitCode = process.waitFor();
        if (exitCode!= 0) {
            compilationErrorMessage = output.toString();
        }
        return exitCode== 0;
    }

    @Override
    public String getCompilationErrorMessage() {
        return compilationErrorMessage;
    }
}

//Rust语言编译器实现
class RustCompiler implements Compiler {
    private String compilationErrorMessage;
    @Override
    public boolean compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("solution.rs");
        Files.write(sourceFile, sourceCode.getBytes());

        // 编译C代码
        ProcessBuilder pb = new ProcessBuilder("rustc", "-o", "solution", sourceFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 获取编译输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            compilationErrorMessage = output.toString();
            return false;
        }
        return true;
    }
    @Override
    public String getCompilationErrorMessage() {
        return compilationErrorMessage;
    }
}

//KotlinCompiler实现
class KotlinCompiler implements Compiler {
    private String compilationErrorMessage;

    @Override
 public boolean compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("Main.kt");
        Files.write(sourceFile, sourceCode.getBytes());

        // 编译 Kotlin 代码
        ProcessBuilder pb = new ProcessBuilder("kotlinc", sourceFile.toString(), "-include-runtime", "-d", "Main.jar");
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 获取编译输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            compilationErrorMessage = output.toString();
            return false;
        }
        return true;
    }

    @Override
    public String getCompilationErrorMessage() {
        return compilationErrorMessage;
    }
}

///**
// * JavaScript语言编译器实现
// */
//class JavaScriptCompiler implements Compiler {
//    private String compilationErrorMessage;
//    @Override
//    public boolean compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
//        // 创建源代码文件
//        Path sourceFile = workDir.resolve("solution.js");
//        Files.write(sourceFile, sourceCode.getBytes());
//        return true;
//    }
//    @Override
//    public String getCompilationErrorMessage() {
//        return compilationErrorMessage;
//    }
//}

/**
 * 代码执行器接口
 */
interface CodeExecutor {
    CaseResult execute(TestCase testCase, Path workDir,int times) throws IOException, InterruptedException;
}

/**
 * 沙盒执行器实现 - 使用Firejail
 */
class FirejailExecutor implements CodeExecutor {
    private Language language;
    private static final String FIREJAIL_CMD = "firejail";

    public FirejailExecutor(Language language) {
        this.language = language;
    }

    @Override
    public synchronized CaseResult execute(TestCase testCase, Path workDir,int times) throws IOException, InterruptedException {
        String[] command;
        switch (language) {
            case JAVA:
                command = new String[]{
                        FIREJAIL_CMD, "--quiet", "--private=" + workDir, "--seccomp",
                        "--net=none","--cpu=0","--nogroups","--nonewprivs","--caps.drop=all",
                        "java",  "-cp", workDir.toString(), "Main"};
                break;
            case PYTHON:
                command = new String[]{FIREJAIL_CMD, "--quiet", "--private=" + workDir, "--seccomp",
                        "--net=none", "--rlimit-as=" + testCase.getMemoryLimit() * 1024 * 1024,
                        "python3", workDir.resolve("solution.py").toString()};
                break;
            case CPP:
            case C, RUST:
                command = new String[]{FIREJAIL_CMD, "--quiet", "--private=" + workDir, "--seccomp",
                        "--net=none",
                        workDir.resolve("solution").toString()};
                break;
            case GO:
                command = new String[]{FIREJAIL_CMD, "--quiet", "--private=" + workDir, "--seccomp",
                        "--net=none",
                        "go", "run", workDir.resolve("solution.go").toString()};
                break;
            case PHP:
                command = new String[]{FIREJAIL_CMD, "--quiet", "--private=" + workDir, "--seccomp",
                        "--net=none",
                        "php", workDir.resolve("solution.php").toString()};
                break;
            case KOTLIN:
                command = new String[]{
                        FIREJAIL_CMD, "--quiet", "--private=" + workDir, "--seccomp",
                        "--net=none","--cpu=0","--nogroups","--nonewprivs","--caps.drop=all",
                        "java", "-jar", workDir.resolve("Main.jar").toString()
                };
                break;
            default:
                return new CaseResult(SimpleResult.SYSTEM_ERROR, "Unsupported language", 0, 0, "");
        }

        // 记录开始时间
        long startTime = System.currentTimeMillis();

//        // 创建进程
//        ProcessBuilder pb = new ProcessBuilder(command);
//
//        pb.directory(workDir.toFile());
//
//        // 将测试输入写入到临时文件
//        Path inputFile = workDir.resolve("input"+times+".txt");
//        Files.write(inputFile, testCase.getInput().getBytes());
//
//        // 设置输入和输出
//        pb.redirectInput(inputFile.toFile());
//
//        // 创建输出文件
//        Path outputFile = workDir.resolve("output"+times+".txt");
//        pb.redirectOutput(outputFile.toFile());
//
//        // 创建错误输出文件
//        Path errorFile = workDir.resolve("error"+times+".txt");
//        pb.redirectError(errorFile.toFile());
//
//        // 启动进程
//        Process process = pb.start();
//
//        long pid = process.pid();
//
//        // 估算内存使用
//        double memoryUsed = estimateMemoryUsage(pid);
//
//        // 设置超时
//        boolean completed = process.waitFor(testCase.getTimeLimit(), TimeUnit.MILLISECONDS);
//
//        // 记录结束时间
//        long endTime = System.currentTimeMillis();
//        long executionTime = endTime - startTime;
//
////        // 强制终止进程（如果还在运行）
////        if (!completed) {
////            process.destroyForcibly();
////            process.waitFor(1, TimeUnit.SECONDS); // 给进程一点时间来终止
////            return new CaseResult(SimpleResult.TIME_LIMIT_EXCEEDED,
////                    "Time limit exceeded: " + testCase.getTimeLimit() + "ms",
////                    testCase.getTimeLimit(),
////                    memoryUsed,
////                    "");
////        }
//
//        // 读取程序输出
//        String actualOutput = new String(Files.readAllBytes(outputFile));
//
//        // 读取错误输出
//        String errorOutput = new String(Files.readAllBytes(errorFile));
//
//        // 获取退出代码
//        int exitCode = process.exitValue();

        ProcessBuilder pb = new ProcessBuilder(command); // 替换为实际命令
        pb.redirectErrorStream(true); // 合并标准输出和错误输出

        // 启动进程
        Process process = pb.start();

        // 写入输入到进程的标准输入流
        try (OutputStream stdin = process.getOutputStream()) {
            stdin.write(testCase.getInput().getBytes(StandardCharsets.UTF_8));
            stdin.flush();
        }

        // 估算内存使用（假设 estimateMemoryUsage 是一个已定义的方法）
        double memoryUsed = estimateMemoryUsage(process.pid());

        // 设置超时
        boolean completed = process.waitFor(12, TimeUnit.SECONDS);

        // 强制终止进程（如果还在运行）
        if (!completed) {
            process.destroyForcibly();
            process.waitFor(1, TimeUnit.SECONDS); // 给进程一点时间来终止
            return new CaseResult(SimpleResult.TIME_LIMIT_EXCEEDED,
                    "Time limit exceeded: " + 10 + "ms",
                    10,
                    memoryUsed,
                    "");
        }

        // 记录结束时间
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // 捕获标准输出和错误输出
        StringBuilder outputBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();
        try (InputStream stdout = process.getInputStream();
             InputStream stderr = process.getErrorStream()) {

            // 读取标准输出
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = stdout.read(buffer)) != -1) {
                outputBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }

            // 读取错误输出（如果有）
            while ((bytesRead = stderr.read(buffer)) != -1) {
                errorBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
        }

        // 获取退出代码
        int exitCode = process.exitValue();

        // 返回结果
        String actualOutput = outputBuilder.toString();
        String errorOutput = errorBuilder.toString();

        // 检查内存限制
        if (memoryUsed > testCase.getMemoryLimit()) {
            return new CaseResult(SimpleResult.MEMORY_LIMIT_EXCEEDED,
                    "Memory limit exceeded: " + memoryUsed + "MB used, limit is " + testCase.getMemoryLimit() + "MB",
                    executionTime,
                    memoryUsed,
                    actualOutput);
        }

        // 检查运行时错误
        if (exitCode != 0 || !errorOutput.isEmpty()) {
            return new CaseResult(SimpleResult.RUNTIME_ERROR,
                    "Runtime error (exit code " + exitCode + "): " + errorOutput,
                    executionTime,
                    memoryUsed,
                    actualOutput);
        }

        // 比较输出
        if (compareOutput(actualOutput, testCase.getExpectedOutput())) {
            return new CaseResult(SimpleResult.ACCEPTED,
                    "Correct answer",
                    executionTime,
                    memoryUsed,
                    actualOutput);
        } else {
            return new CaseResult(SimpleResult.WRONG_ANSWER,
                    "Expected: '" + testCase.getExpectedOutput() + "', but got: '" + actualOutput + "'",
                    executionTime,
                    memoryUsed,
                    actualOutput);
        }
    }

    @NotNull
    private static Result getResult(TestCase testCase, Path workDir, String[] command,int times) throws IOException {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 创建进程
        ProcessBuilder pb = new ProcessBuilder(command);

        pb.directory(workDir.toFile());

        // 将测试输入写入到临时文件
        Path inputFile = workDir.resolve("input"+times+".txt");
        Files.write(inputFile, testCase.getInput().getBytes());

        // 设置输入和输出
        pb.redirectInput(inputFile.toFile());

        // 创建输出文件
        Path outputFile = workDir.resolve("output"+times+".txt");
        pb.redirectOutput(outputFile.toFile());

        // 创建错误输出文件
        Path errorFile = workDir.resolve("error"+times+".txt");
        pb.redirectError(errorFile.toFile());

        // 启动进程
        Process process = pb.start();

        long pid = process.pid();
        return new Result(startTime, outputFile, errorFile, process, pid);
    }

//    private static Result getResult2(TestCase testCase, Path workDir, String[] command) throws IOException{
//        System.out.println(testCase.getInput()+" "+testCase.getExpectedOutput());
//        // 创建进程
//        ProcessBuilder pb = new ProcessBuilder(command);
//
//        pb.directory(workDir.toFile());
//        // 记录开始时间
//        long startTime = System.currentTimeMillis();
//        // 创建输出文件
//        Path outputFile = workDir.resolve("output.txt");
//        pb.redirectOutput(outputFile.toFile());
//
//        // 创建错误输出文件
//        Path errorFile = workDir.resolve("error.txt");
//        pb.redirectError(errorFile.toFile());
//        // 启动进程
//        Process process = pb.start();
//        process.getOutputStream().write(testCase.getInput().getBytes());
//        process.getOutputStream().flush();
//        process.getOutputStream().close();
//        long pid = process.pid();
//
//        return new Result(startTime, outputFile, errorFile, process, pid);
//    }

    private static class Result {
        public final long startTime;
        public final Path outputFile;
        public final Path errorFile;
        public final Process process;
        public final long pid;

        public Result(long startTime, Path outputFile, Path errorFile, Process process, long pid) {
            this.startTime = startTime;
            this.outputFile = outputFile;
            this.errorFile = errorFile;
            this.process = process;
            this.pid = pid;
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
        return output.trim();
    }

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
            if (exitCode != 0) {
                 System.err.println("查询内存命令执行失败，退出码: " + exitCode);
            } else if (memoryUsageStr != null && !memoryUsageStr.trim().isEmpty()) {
                try {
                    // ps 输出的 RSS 单位通常是 KiB
                    memoryUsageKiB = Long.parseLong(memoryUsageStr.trim());
                    System.out.printf("进程 %d 的内存占用 (RSS): %d KiB%n", pid, memoryUsageKiB);
                } catch (NumberFormatException e) {
                    System.err.println("无法解析内存使用情况: " + memoryUsageStr);
                }
            } else {
                 System.err.println("无法获取进程 " + pid + " 的内存信息 (进程可能已退出或命令无输出)。");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return memoryUsageKiB>>10; // 将 KiB 转换为 MB;
    }
    private static String readStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            output.append(line).append("\n");
        }

        return output.toString();
    }
}

/**
 * 判题服务核心实现
 */
class JudgeService {

    ThreadPoolExecutor executorService=new ThreadPoolExecutor(
            10, // 核心线程数
            20, // 最大线程数
            10, // 线程空闲时间（秒）
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100), // 任务队列
            Executors.defaultThreadFactory(), // 线程工厂
            new ThreadPoolExecutor.AbortPolicy() // 拒绝策略
    );

    /**
     * 主判题方法，处理一个完整的提交
     */
    public JudgeResult judge(List<TestCase> testCases, String sourceCode, Language language) {
        Path tempDir = null;
        try {
            // 创建临时工作目录
            tempDir = Files.createTempDirectory("judge_");

            // 编译代码
            Compiler compiler = getCompiler(language);
            boolean compilationSuccess = compiler.compile(sourceCode, tempDir);

            if (!compilationSuccess) {
                return new JudgeResult(
                    SimpleResult.COMPILATION_ERROR,
                    compiler.getCompilationErrorMessage(),
                    0,
                    0,
                    Collections.emptyList()
                );
            }

            // 配置执行器
            CodeExecutor executor = new FirejailExecutor(language);

            //顺序执行
            List<CaseResult> caseResults = getCaseResults(testCases, tempDir, executor);

            // 汇总结果
            return summarizeResults(caseResults);

        } catch (Exception e) {
            return new JudgeResult(
                SimpleResult.SYSTEM_ERROR,
                "Judge system error: " + e.getMessage(),
                0,
                0,
                Collections.emptyList()
            );
        } finally {
            // 清理临时目录
            cleanupTempDir(tempDir);
        }
    }

    //并行执行所有测试用例
    @NotNull
    private List<CaseResult> getCaseResults(List<TestCase> testCases, Path tempDir, CodeExecutor executor) {
        // 并行执行所有测试用例
        List<Future<CaseResult>> futures = new ArrayList<>();
        for (int i = 0; i < testCases.size(); i++) {
            int finalI = i;
            futures.add(executorService.submit(() -> executor.execute(testCases.get(finalI), tempDir, finalI)));
        }

        // 收集结果
        List<CaseResult> caseResults = new ArrayList<>();
        for (Future<CaseResult> future : futures) {
            try {
                caseResults.add(future.get());
            } catch (Exception e) {
                caseResults.add(new CaseResult(
                    SimpleResult.SYSTEM_ERROR,
                    "Error executing test case: " + e.getMessage(),
                    0,
                    0,
                    ""
                ));
            }
        }
        return caseResults;
    }

    // 顺序执行所有测试用例
    @NotNull
    private List<CaseResult> getCaseResultsSequential(List<TestCase> testCases, Path tempDir, CodeExecutor executor) {
        List<CaseResult> caseResults = new ArrayList<>();
        for (TestCase testCase : testCases) {
            try {
                CaseResult result = executor.execute(testCase, tempDir,0);
                caseResults.add(result);
            }catch (Exception e) {
                caseResults.add(new CaseResult(
                    SimpleResult.SYSTEM_ERROR,
                    "Error executing test case: " + e.getMessage(),
                    0,
                    0,
                    ""
                ));
            }
        }
        return caseResults;
    }

    private Compiler getCompiler(Language language) {
        switch (language) {
            case JAVA:
                return new JavaCompiler();
            case PYTHON:
                return new PythonCompiler();
            case CPP:
                return new CppCompiler();
            case C:
                return new CCompiler();
            case GO:
                return new GoCompiler();
//            case JAVASCRIPT:
//                return new JavaScriptCompiler();
//            case RUBY:
//                return new RubyCompiler();
            case PHP:
                return new PHPCompiler();
            case RUST:
                return new RustCompiler();
            case KOTLIN:
                return new KotlinCompiler();
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }

    private JudgeResult summarizeResults(List<CaseResult> caseResults) {
        if (caseResults.isEmpty()) {
            return new JudgeResult(
                SimpleResult.SYSTEM_ERROR,
                "No test cases were executed",
                0,
                0,
                Collections.emptyList()
            );
        }

        // 统计不同状态的测试用例数量
        Map<SimpleResult, Long> statusCounts = caseResults.stream()
                .collect(Collectors.groupingBy(CaseResult::getStatus, Collectors.counting()));

        // 计算总执行时间和最大内存使用
        long totalExecutionTime = caseResults.stream().mapToLong(CaseResult::getExecutionTime).sum();
        double maxMemoryUsed = caseResults.stream().mapToDouble(CaseResult::getMemoryUsed).max().orElse(0);

        // 确定最终状态（按优先级）
        SimpleResult finalStatus;
        String message;

        if (statusCounts.containsKey(SimpleResult.SYSTEM_ERROR)) {
            finalStatus = SimpleResult.SYSTEM_ERROR;
            message = "System error occurred during execution";
        } else if (statusCounts.containsKey(SimpleResult.COMPILATION_ERROR)) {
            finalStatus = SimpleResult.COMPILATION_ERROR;
            message = "Compilation error";
        } else if (statusCounts.containsKey(SimpleResult.RUNTIME_ERROR)) {
            finalStatus = SimpleResult.RUNTIME_ERROR;
            message = "Runtime error in " + statusCounts.get(SimpleResult.RUNTIME_ERROR) + " test case(s)";
        } else if (statusCounts.containsKey(SimpleResult.MEMORY_LIMIT_EXCEEDED)) {
            finalStatus = SimpleResult.MEMORY_LIMIT_EXCEEDED;
            message = "Memory limit exceeded in " + statusCounts.get(SimpleResult.MEMORY_LIMIT_EXCEEDED) + " test case(s)";
        } else if (statusCounts.containsKey(SimpleResult.TIME_LIMIT_EXCEEDED)) {
            finalStatus = SimpleResult.TIME_LIMIT_EXCEEDED;
            message = "Time limit exceeded in " + statusCounts.get(SimpleResult.TIME_LIMIT_EXCEEDED) + " test case(s)";
        } else if (statusCounts.containsKey(SimpleResult.WRONG_ANSWER)) {
            finalStatus = SimpleResult.WRONG_ANSWER;
            message = "Wrong answer in " + statusCounts.get(SimpleResult.WRONG_ANSWER) + " test case(s)";
        } else {
            finalStatus = SimpleResult.ACCEPTED;
            message = "All test cases passed";
        }

        return new JudgeResult(
            finalStatus,
            message,
            totalExecutionTime,
            maxMemoryUsed,
            caseResults
        );
    }

    private void cleanupTempDir(Path tempDir) {
        if (tempDir != null) {
            try {
                // 递归删除目录
                Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path + ", " + e.getMessage());
                        }
                    });
            } catch (IOException e) {
                System.err.println("Error cleaning up temp directory: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}

/**
 * RPC服务接口 - 用于接收远程调用
 */
interface JudgeRpcService {
    JudgeResult judge(List<TestCase> testCases, String sourceCode, Language language);
}

/**
 * RPC服务实现
 */
class JudgeRpcServiceImpl implements JudgeRpcService {
    private final JudgeService judgeService;

    public JudgeRpcServiceImpl() {
        this.judgeService = new JudgeService();
    }

    @Override
    public JudgeResult judge(List<TestCase> testCases, String sourceCode, Language language) {
        // 记录判题请求
        System.out.println("Received judge request: " + testCases.size() + " test cases, language: " + language);

        // 执行判题
        JudgeResult result = judgeService.judge(testCases, sourceCode, language);

        // 记录判题结果
        System.out.println("Judge completed with status: " + result.getStatus() + ", message: " + result.getMessage());

        return result;
    }

    public void shutdown() {
        judgeService.shutdown();
    }
}

/**
 * 判题机主类
 */
public class JudgeSystem {
    public static void main(String[] args) {
        // 创建RPC服务
        JudgeRpcServiceImpl rpcService = new JudgeRpcServiceImpl();

        // 注册关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down judge service...");
            rpcService.shutdown();
        }));

        // 启动RPC服务器（这里需要根据实际RPC框架实现）
        System.out.println("Judge service started and ready to accept requests...");

        // TODO: 在这里实现RPC服务器的启动代码，例如使用gRPC、Thrift等

        // 示例：手动测试判题功能

        runTestExample(rpcService);

    }


    private static void runTestExample(JudgeRpcServiceImpl rpcService)  {
        System.out.println("Running test example...");

        // 创建测试用例
        List<TestCase> testCases = new ArrayList<>();
        testCases.add(new TestCase("1 2\n", "3\n", 2000, 64));
        testCases.add(new TestCase("5 7\n", "12\n", 2000, 64));
        testCases.add(new TestCase("1000 1000\n", "2000\n", 2000, 64));
        testCases.add(new TestCase("1000000 1000000\n", "2000000\n", 2000, 64));

////        // Java示例代码 - 正确答案
//        String javaCode =
//            "import java.util.Scanner;\n" +
//            "public class Main {\n" +
//            "    public static void main(String[] args) {\n" +
//            "        Scanner scanner = new Scanner(System.in);\n" +
//            "        int a = scanner.nextInt();\n" +
//            "        int b = scanner.nextInt();\n" +
//            "        //throw new RuntimeException(\"test\"); \n"+
//            "        System.out.println(a + b);\n" +
//            "    }\n" +
//            "}";
//
//        // 执行判题
//        JudgeResult result = rpcService.judge(testCases, javaCode, Language.JAVA);
//
//        String cppCode =
//            "#include <iostream>\n" +
//            "int main() {\n" +
//            "    int a, b;\n" +
//            "    std::cin >> a >> b;\n" +
//            "    std::cout << a + b << std::endl;\n" +
//            "    return 0;\n" +
//            "}";
//
//        JudgeResult result = rpcService.judge(testCases, cppCode, Language.CPP);

//        String pythonCode =
//            "a, b = map(int, input().split())\n" +
//            "print(a + b)";
//
//        JudgeResult result = rpcService.judge(testCases, pythonCode, Language.PYTHON);
//
//        String cCode =
//                """
//                #include <stdio.h>
//                int main() {
//                    int a, b;
//                    scanf("%d %d", &a, &b);
//                    printf("%d\\n", a + b);
//                    return 0;
//                }
//                """;
//
//        JudgeResult result = rpcService.judge(testCases, cCode, Language.C);
//        String goCode =
//            "package main\n" +
//            "import \"fmt\"\n" +
//            "func main() {\n" +
//            "    var a, b int\n" +
//            "    fmt.Scan(&a, &b)\n" +
//            "    fmt.Println(a + b)\n" +
//            "}";
//        JudgeResult result = rpcService.judge(testCases, goCode, Language.GO);

//
//        String javascriptCode =
//            "const readline = require('readline');\n" +
//            "const rl = readline.createInterface({\n" +
//            "    input: process.stdin,\n" +
//            "    output: process.stdout\n" +
//            "});\n" +
//            "rl.question(\"\", (input) => {\n" +
//            "    const [a, b] = input.split(' ').map(Number);\n" +
//            "    console.log(a + b);\n" +
//            "    rl.close();\n" +
//            "});";
//        JudgeResult result = rpcService.judge(testCases, javascriptCode, Language.JAVASCRIPT);

//        String phpCode =
//            "<?php\n" +
//            "fscanf(STDIN, \"%d %d\", $a, $b);\n" +
//            "echo $a + $b;\n";
//        JudgeResult result = rpcService.judge(testCases, phpCode, Language.PHP);

//
//        String rustCode ="""
//            use std::io;
//            fn main() {
//                let mut input = String::new();
//                io::stdin().read_line(&mut input).expect("读取输入失败");
//
//                // 解析输入
//                let numbers: Vec<i32> = input
//                    .split_whitespace()
//                    .map(|s| s.parse().expect("解析失败"))
//                    .collect();
//
//                // 确保输入了两个数字
//                if numbers.len() == 2 {
//                    let a = numbers[0];
//                    let b = numbers[1];
//
//                    // 输出结果
//                    println!("{}", a + b);
//                } else {
//                    println!("请输入两个整数！");
//                }
//            }
//        """;
//        JudgeResult result = rpcService.judge(testCases, rustCode, Language.RUST);

        String kotlinCode = """
            fun main() {
                val (a, b) = readLine()!!.split(" ").map { it.toInt() }
                println(a + b)
            }
        """;
        JudgeResult result = rpcService.judge(testCases, kotlinCode, Language.KOTLIN);

        // 打印结果
        System.out.println("Test Result:");
        System.out.println("Status: " + result.getStatus());
        System.out.println("Message: " + result.getMessage());
        System.out.println("Execution Time: " + result.getExecutionTime() + "ms");
        System.out.println("Memory Used: " + result.getMemoryUsed() + "MB");
        System.out.println("Case Results:");

        for (int i = 0; i < result.getCaseResults().size(); i++) {
            CaseResult caseResult = result.getCaseResults().get(i);
            System.out.println("  Case #" + (i + 1) + ":");
            System.out.println("    Status: " + caseResult.getStatus());
            System.out.println("    Message: " + caseResult.getMessage());
            System.out.println("    Execution Time: " + caseResult.getExecutionTime() + "ms");
            System.out.println("    Memory Used: " + caseResult.getMemoryUsed() + "MB");
            System.out.println("    Actual Output: '" + caseResult.getActualOutput() + "'");
        }
    }
}
