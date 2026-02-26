package org.laoli.judge.service.test.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.dto.CodeTestRequest;
import org.laoli.judge.model.dto.CodeTestResponse;
import org.laoli.judge.model.dto.CodeTestResponse.CaseTestResult;
import org.laoli.judge.model.dto.CodeTestResponse.TestStatus;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.service.compile.Compiler;
import org.laoli.judge.service.compile.CompilerFactory;
import org.laoli.judge.service.execute.CodeExecutor;
import org.laoli.judge.service.execute.LanguageCommandFactory;
import org.laoli.judge.service.test.ICodeTestService;
import org.laoli.judge.service.validation.InputValidator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 代码测试服务实现类
 * 模拟LeetCode的测试机制
 *
 * 核心功能:
 * 1. 执行用户提交的代码片段
 * 2. 支持有输入/输出的测试用例验证
 * 3. 支持无输入仅执行代码的场景
 * 4. 批量运行所有预设测试用例
 * 5. 返回每个测试用例的详细执行结果和比对信息
 *
 * LeetCode风格:
 * - 每个测试用例独立执行
 * - 返回预期输出 vs 实际输出
 * - 显示执行时间和内存使用
 * - 支持显示详细输出模式
 *
 * @author laoli
 * @date 2025/02/26
 */
@Slf4j
@Service
public class CodeTestService implements ICodeTestService {

    /** 线程池执行器，用于并发执行测试用例 */
    @Resource
    private ThreadPoolExecutor executorService;

    /** 代码执行器 */
    @Resource
    private CodeExecutor executor;

    /** 语言命令工厂 */
    @Resource
    private LanguageCommandFactory languageCommandFactory;

    /** 编译器工厂 */
    @Resource
    private CompilerFactory compilerFactory;

    /** 输入验证器 */
    @Resource
    private InputValidator inputValidator;

    /** 临时文件目录前缀 */
    private static final String TEMP_DIR_PREFIX = "codetest_";

    /**
     * 执行代码测试 (批量测试用例)
     *
     * 处理流程:
     * 1. 参数预处理和验证
     * 2. 编译用户代码
     * 3. 逐个执行测试用例并记录结果
     * 4. 汇总所有测试结果
     *
     * @param request 代码测试请求
     * @return 包含所有测试用例执行结果的响应
     */
    @Override
    public CodeTestResponse executeTest(CodeTestRequest request) {
        // Step 1: 参数预处理
        preprocessRequest(request);

        // Step 2: 验证输入参数
        if (!validateRequest(request)) {
            return CodeTestResponse.buildError(
                    TestStatus.SYSTEM_ERROR,
                    "Invalid request: " + request.getClass().getSimpleName());
        }

        // 检查是否需要执行测试用例
        if (request.getTestCases() == null || request.getTestCases().isEmpty()) {
            return executeCodeOnly(request);
        }

        // Step 3: 编译代码
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
            log.debug("Created temp directory for code test: {}", tempDir);

            // 编译用户代码
            Compiler compiler = compilerFactory.getCompiler(parseLanguage(request.getLanguage()));
            if (compiler == null) {
                return CodeTestResponse.buildError(
                        TestStatus.COMPILE_ERROR,
                        "Unsupported language: " + request.getLanguage());
            }

            var compileResult = compiler.compile(request.getCode(), tempDir);
            if (compileResult != null && compileResult.status() != SimpleResult.ACCEPTED) {
                return CodeTestResponse.buildError(
                        TestStatus.COMPILE_ERROR,
                        compileResult.message() != null ? compileResult.message() : "Compilation failed");
            }

            // Step 4: 执行所有测试用例
            String[] command = languageCommandFactory.getCommand(
                    parseLanguage(request.getLanguage()),
                    tempDir);

            List<CaseTestResult> caseResults = executeTestCases(
                    request.getTestCases(),
                    tempDir,
                    command,
                    request.getTimeLimit(),
                    request.getMemoryLimit(),
                    request.getShowDetail());

            // Step 5: 汇总结果
            return summarizeResults(caseResults, request.getShowDetail());

        } catch (IOException e) {
            log.error("IO error during code test: {}", e.getMessage(), e);
            return CodeTestResponse.buildError(TestStatus.SYSTEM_ERROR, "IO error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during code test: {}", e.getMessage(), e);
            return CodeTestResponse.buildError(TestStatus.SYSTEM_ERROR, "Error: " + e.getMessage());
        } finally {
            cleanupTempDir(tempDir);
        }
    }

    /**
     * 执行单个测试用例
     * 用于调试或单独运行某个测试
     *
     * @param request 代码测试请求 (仅使用第一个测试用例)
     * @return 单个测试用例的执行结果
     */
    @Override
    public CodeTestResponse executeSingleTest(CodeTestRequest request) {
        if (request.getTestCases() == null || request.getTestCases().isEmpty()) {
            return executeCodeOnly(request);
        }

        // 仅取第一个测试用例
        CodeTestRequest.CodeTestCase firstCase = request.getTestCases().get(0);
        List<CodeTestRequest.CodeTestCase> singleCase = List.of(firstCase);
        request.setTestCases(singleCase);

        return executeTest(request);
    }

    /**
     * 预处理请求参数
     * 设置默认值，确保参数有效性
     *
     * @param request 输入请求
     */
    private void preprocessRequest(CodeTestRequest request) {
        // 设置默认时间限制
        if (request.getTimeLimit() == null || request.getTimeLimit() < 100) {
            request.setTimeLimit(1000L);
        }

        // 设置默认内存限制
        if (request.getMemoryLimit() == null || request.getMemoryLimit() < 1024) {
            request.setMemoryLimit(4096L);
        }

        // 设置默认显示详细输出
        if (request.getShowDetail() == null) {
            request.setShowDetail(false);
        }
    }

    /**
     * 验证请求参数
     *
     * @param request 待验证的请求
     * @return 验证是否通过
     */
    private boolean validateRequest(CodeTestRequest request) {
        // 验证代码不为空
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            log.warn("Code is empty");
            return false;
        }

        // 验证语言有效
        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
            log.warn("Language is empty");
            return false;
        }

        try {
            parseLanguage(request.getLanguage());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid language: {}", request.getLanguage());
            return false;
        }

        return true;
    }

    /**
     * 仅执行代码，不进行测试验证
     * 用于无测试用例或纯执行场景
     *
     * @param request 代码测试请求
     * @return 执行结果
     */
    private CodeTestResponse executeCodeOnly(CodeTestRequest request) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);

            // 编译代码
            Compiler compiler = compilerFactory.getCompiler(parseLanguage(request.getLanguage()));
            if (compiler == null) {
                return CodeTestResponse.buildError(
                        TestStatus.COMPILE_ERROR,
                        "Unsupported language: " + request.getLanguage());
            }

            var compileResult = compiler.compile(request.getCode(), tempDir);
            if (compileResult != null && compileResult.status() != SimpleResult.ACCEPTED) {
                return CodeTestResponse.buildError(
                        TestStatus.COMPILE_ERROR,
                        compileResult.message() != null ? compileResult.message() : "Compilation failed");
            }

            // 执行代码 (使用空输入)
            String[] command = languageCommandFactory.getCommand(
                    parseLanguage(request.getLanguage()),
                    tempDir);

            TestCase emptyTestCase = TestCase.builder()
                    .input("")
                    .expectedOutput("")
                    .build();

            var caseResult = executor.execute(
                    emptyTestCase,
                    tempDir,
                    command,
                    request.getTimeLimit(),
                    request.getMemoryLimit());

            // 返回执行结果
            if (caseResult.status() == SimpleResult.TIME_LIMIT_EXCEEDED) {
                return CodeTestResponse.buildError(TestStatus.TIME_LIMIT_EXCEEDED, "Execution timed out");
            }

            if (caseResult.status() == SimpleResult.MEMORY_LIMIT_EXCEEDED) {
                return CodeTestResponse.buildError(TestStatus.MEMORY_LIMIT_EXCEEDED, "Memory limit exceeded");
            }

            if (caseResult.status() == SimpleResult.RUNTIME_ERROR) {
                return CodeTestResponse.buildError(
                        TestStatus.RUNTIME_ERROR,
                        caseResult.message() != null ? caseResult.message() : "Runtime error");
            }

            return CodeTestResponse.buildExecutedOnly(
                    caseResult.executionTime(),
                    caseResult.memoryUsed() != null ? caseResult.memoryUsed() : 0L,
                    caseResult.actualOutput());

        } catch (Exception e) {
            log.error("Error during code-only execution: {}", e.getMessage(), e);
            return CodeTestResponse.buildError(TestStatus.SYSTEM_ERROR, "Error: " + e.getMessage());
        } finally {
            cleanupTempDir(tempDir);
        }
    }

    /**
     * 执行所有测试用例
     *
     * @param testCases   测试用例列表
     * @param tempDir     临时工作目录
     * @param command     执行命令
     * @param timeLimit   时间限制 (毫秒)
     * @param memoryLimit 内存限制 (KB)
     * @param showDetail  是否显示详细输出
     * @return 每个测试用例的执行结果
     */
    private List<CaseTestResult> executeTestCases(
            List<CodeTestRequest.CodeTestCase> testCases,
            Path tempDir,
            String[] command,
            Long timeLimit,
            Long memoryLimit,
            Boolean showDetail) {
        List<CaseTestResult> results = new ArrayList<>();

        for (int i = 0; i < testCases.size(); i++) {
            CodeTestRequest.CodeTestCase testCase = testCases.get(i);

            try {
                // 构建测试用例实体
                TestCase caseEntity = TestCase.builder()
                        .input(testCase.getInput() != null ? testCase.getInput() : "")
                        .expectedOutput(testCase.getExpectedOutput() != null ? testCase.getExpectedOutput() : "")
                        .build();

                // 执行测试
                var execResult = executor.execute(
                        caseEntity,
                        tempDir,
                        command,
                        timeLimit,
                        memoryLimit);

                // 构建测试结果
                CaseTestResult result = buildCaseTestResult(
                        testCase,
                        i,
                        execResult,
                        showDetail);
                results.add(result);

            } catch (Exception e) {
                log.error("Error executing test case {}: {}", i, e.getMessage());
                results.add(buildErrorCaseResult(testCase, i, e.getMessage(), showDetail));
            }
        }

        return results;
    }

    /**
     * 构建测试用例结果
     *
     * @param testCase   原始测试用例
     * @param index      测试用例索引
     * @param execResult 执行结果
     * @param showDetail 是否显示详细输出
     * @return 测试用例结果
     */
    private CaseTestResult buildCaseTestResult(
            CodeTestRequest.CodeTestCase testCase,
            int index,
            org.laoli.judge.model.entity.CaseResult execResult,
            Boolean showDetail) {
        TestStatus status = mapToTestStatus(execResult.status());

        // 如果预期输出为空，仅返回执行结果状态
        if (testCase.getExpectedOutput() == null || testCase.getExpectedOutput().isEmpty()) {
            return CaseTestResult.builder()
                    .caseId(testCase.getId() != null ? testCase.getId() : "case_" + index)
                    .index(index)
                    .description(testCase.getDescription())
                    .input(showDetail ? testCase.getInput() : null)
                    .expectedOutput(null)
                    .actualOutput(showDetail ? execResult.actualOutput() : null)
                    .status(status)
                    .executionTime(execResult.executionTime())
                    .memoryUsed(execResult.memoryUsed() != null ? execResult.memoryUsed() : 0L)
                    .errorMessage(execResult.message())
                    .build();
        }

        // 比对预期输出与实际输出
        boolean isCorrect = compareOutput(testCase.getExpectedOutput(), execResult.actualOutput());

        return CaseTestResult.builder()
                .caseId(testCase.getId() != null ? testCase.getId() : "case_" + index)
                .index(index)
                .description(testCase.getDescription())
                .input(showDetail ? testCase.getInput() : null)
                .expectedOutput(showDetail ? testCase.getExpectedOutput() : null)
                .actualOutput(showDetail ? execResult.actualOutput() : null)
                .status(isCorrect ? TestStatus.ALL_PASSED : TestStatus.ALL_FAILED)
                .executionTime(execResult.executionTime())
                .memoryUsed(execResult.memoryUsed() != null ? execResult.memoryUsed() : 0L)
                .errorMessage(isCorrect ? null : "Output mismatch")
                .build();
    }

    /**
     * 构建错误测试结果
     */
    private CaseTestResult buildErrorCaseResult(
            CodeTestRequest.CodeTestCase testCase,
            int index,
            String errorMessage,
            Boolean showDetail) {
        return CaseTestResult.builder()
                .caseId(testCase.getId() != null ? testCase.getId() : "case_" + index)
                .index(index)
                .description(testCase.getDescription())
                .input(showDetail ? testCase.getInput() : null)
                .expectedOutput(showDetail ? testCase.getExpectedOutput() : null)
                .actualOutput(null)
                .status(TestStatus.RUNTIME_ERROR)
                .executionTime(0L)
                .memoryUsed(0L)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 汇总测试结果
     *
     * @param caseResults 所有测试用例结果
     * @param showDetail  是否显示详细输出
     * @return 最终响应
     */
    private CodeTestResponse summarizeResults(List<CaseTestResult> caseResults, Boolean showDetail) {
        // 计算统计数据
        long passedCount = caseResults.stream()
                .filter(r -> r.getStatus() == TestStatus.ALL_PASSED)
                .count();

        long totalTime = caseResults.stream()
                .mapToLong(r -> r.getExecutionTime() != null ? r.getExecutionTime() : 0L)
                .sum();

        long maxMemory = caseResults.stream()
                .mapToLong(r -> r.getMemoryUsed() != null ? r.getMemoryUsed() : 0L)
                .max()
                .orElse(0L);

        // 根据通过情况返回相应响应
        if (passedCount == caseResults.size()) {
            return CodeTestResponse.buildAllPassed(caseResults, totalTime, maxMemory);
        } else if (passedCount == 0) {
            return CodeTestResponse.buildFailed(caseResults, totalTime, maxMemory);
        } else {
            return CodeTestResponse.buildPartialPassed(caseResults, totalTime, maxMemory);
        }
    }

    /**
     * 比较预期输出与实际输出
     * 使用简化的比较逻辑，支持空白符忽略
     *
     * @param expected 预期输出
     * @param actual   实际输出
     * @return 是否匹配
     */
    private boolean compareOutput(String expected, String actual) {
        if (expected == null && actual == null) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }

        // 规范化输出: 去除首尾空白，统一换行符
        String normalizedExpected = expected.trim().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        String normalizedActual = actual.trim().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");

        return normalizedExpected.equals(normalizedActual);
    }

    /**
     * 将内部状态映射为测试状态
     *
     * @param status 内部状态枚举
     * @return 测试状态枚举
     */
    private TestStatus mapToTestStatus(SimpleResult status) {
        if (status == null) {
            return TestStatus.SYSTEM_ERROR;
        }

        return switch (status) {
            case ACCEPTED -> TestStatus.ALL_PASSED;
            case TIME_LIMIT_EXCEEDED -> TestStatus.TIME_LIMIT_EXCEEDED;
            case MEMORY_LIMIT_EXCEEDED -> TestStatus.MEMORY_LIMIT_EXCEEDED;
            case RUNTIME_ERROR -> TestStatus.RUNTIME_ERROR;
            case WRONG_ANSWER -> TestStatus.ALL_FAILED;
            default -> TestStatus.SYSTEM_ERROR;
        };
    }

    /**
     * 解析语言字符串为枚举
     *
     * @param languageStr 语言字符串
     * @return Language枚举
     */
    private Language parseLanguage(String languageStr) {
        try {
            return Language.valueOf(languageStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 尝试匹配支持的语言列表
            return Language.getSupportLanguage().stream()
                    .map(String::toUpperCase)
                    .filter(l -> l.equalsIgnoreCase(languageStr))
                    .map(l -> {
                        try {
                            return Language.valueOf(l);
                        } catch (IllegalArgumentException ex) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported language: " + languageStr));
        }
    }

    /**
     * 清理临时目录
     *
     * @param tempDir 临时目录路径
     */
    private void cleanupTempDir(Path tempDir) {
        if (tempDir != null) {
            try {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.debug("Failed to delete temp file: {}", path);
                            }
                        });
                log.debug("Cleaned up temp directory: {}", tempDir);
            } catch (IOException e) {
                log.warn("Failed to cleanup temp directory: {}", tempDir);
            }
        }
    }
}
