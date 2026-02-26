package org.laoli.judge.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.service.IJudgeService;
import org.laoli.judge.service.comparator.OutputComparator;
import org.laoli.judge.service.compile.Compiler;
import org.laoli.judge.service.compile.CompilerFactory;
import org.laoli.judge.service.execute.CodeExecutor;
import org.laoli.judge.service.execute.LanguageCommandFactory;
import org.laoli.judge.service.monitor.PerformanceMonitor;
import org.laoli.judge.service.summarize.ISummarize;
import org.laoli.judge.service.validation.InputValidator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 判题服务实现类
 * 实现与LeetCode标准一致的判题逻辑
 *
 * LeetCode判题逻辑特点:
 * 1. 所有测试用例必须通过才返回Accepted
 * 2. 遇到错误立即返回，不继续执行后续用例
 * 3. 返回第一个失败的测试用例信息
 * 4. 累计所有测试用例的执行时间和内存使用
 *
 * @author laoli
 * @date 2025/02/26
 */
@Slf4j
@Service
public class JudgeService implements IJudgeService {

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

    /** 结果汇总器 */
    @Resource
    private ISummarize summarize;

    /** 输入验证器 */
    @Resource
    private InputValidator inputValidator;

    /** 输出比较器 */
    @Resource
    private OutputComparator outputComparator;

    /** 性能监控器 */
    @Resource
    private PerformanceMonitor performanceMonitor;

    /** 临时目录前缀 */
    private static final String JUDGE_TEMP_PREFIX = "judge_";

    /** 最大并发测试用例数量，超过此值改为顺序执行 */
    private static final int MAX_CONCURRENT_CASES = 100;

    /**
     * 主判题方法
     *
     * LeetCode风格处理流程:
     * 1. 输入验证和预处理
     * 2. 代码编译
     * 3. 顺序执行所有测试用例 (LeetCode按顺序执行)
     * 4. 遇到失败立即返回第一个失败的用例信息
     * 5. 所有通过则返回Accepted
     *
     * @param testCases   测试用例列表
     * @param sourceCode  用户源代码
     * @param language    编程语言
     * @param timeLimit   时间限制 (毫秒)
     * @param memoryLimit 内存限制 (KB)
     * @return 判题结果
     */
    @Override
    public JudgeResult judge(List<TestCase> testCases, String sourceCode, Language language, long timeLimit,
            long memoryLimit) {

        // Step 1: 应用默认限制值
        applyDefaultLimits(timeLimit, memoryLimit);

        // Step 2: 输入验证
        JudgeResult validationError = inputValidator.validate(sourceCode, language, testCases, timeLimit, memoryLimit);
        if (validationError != null) {
            log.warn("Input validation failed: {}", validationError.message());
            return validationError;
        }

        // Step 3: 选择执行策略
        // LeetCode风格: 始终按顺序执行，确保测试用例的确定性
        if (testCases.size() > MAX_CONCURRENT_CASES) {
            return processSequentially(testCases, sourceCode, language, timeLimit, memoryLimit);
        }

        // 小数据集也使用顺序执行，保证与LeetCode一致
        return processSequentially(testCases, sourceCode, language, timeLimit, memoryLimit);
    }

    /**
     * 应用默认限制值
     * 确保时间限制和内存限制在合理范围内
     */
    private void applyDefaultLimits(long timeLimit, long memoryLimit) {
        Long[] timeBoxed = { timeLimit };
        Long[] memoryBoxed = { memoryLimit };
        inputValidator.applyDefaultLimits(timeBoxed, memoryBoxed);
    }

    /**
     * 顺序执行测试用例 (LeetCode标准模式)
     *
     * 特点:
     * - 按测试用例顺序依次执行
     * - 遇到失败立即返回
     * - 返回第一个失败的测试用例信息
     * - 统计总执行时间和最大内存使用
     *
     * @param testCases   测试用例列表
     * @param sourceCode  源代码
     * @param language    编程语言
     * @param timeLimit   时间限制
     * @param memoryLimit 内存限制
     * @return 判题结果
     */
    private JudgeResult processSequentially(List<TestCase> testCases, String sourceCode, Language language,
            long timeLimit, long memoryLimit) {

        Path tempDir = null;
        try {
            // Step 1: 获取编译器
            Compiler compiler = compilerFactory.getCompiler(language);
            if (compiler == null) {
                return buildErrorResult(SimpleResult.SYSTEM_ERROR,
                        "Compiler not available for language: " + language);
            }

            // Step 2: 创建临时工作目录
            tempDir = Files.createTempDirectory(JUDGE_TEMP_PREFIX);
            log.debug("Created temp directory: {}", tempDir);

            // Step 3: 编译代码
            JudgeResult compileResult = compileCode(compiler, sourceCode, tempDir);
            if (compileResult != null) {
                return compileResult;
            }

            // Step 4: 准备执行命令
            String[] command = languageCommandFactory.getCommand(language, tempDir);

            // Step 5: 顺序执行每个测试用例 (LeetCode风格)
            return executeTestCasesInOrder(testCases, tempDir, command, timeLimit, memoryLimit);

        } catch (IOException e) {
            log.error("IO error during judge: {}", e.getMessage(), e);
            return buildErrorResult(SimpleResult.SYSTEM_ERROR, "IO error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during judge: {}", e.getMessage(), e);
            return buildErrorResult(SimpleResult.SYSTEM_ERROR, "Unexpected error: " + e.getMessage());
        } finally {
            cleanupTempDir(tempDir);
        }
    }

    /**
     * 按顺序执行测试用例
     *
     * LeetCode核心逻辑:
     * 1. 依次执行每个测试用例
     * 2. 如果某个用例失败，立即返回该用例的信息
     * 3. 如果所有用例通过，返回Accepted
     * 4. 统计总执行时间和最大内存使用
     *
     * @param testCases   测试用例列表
     * @param tempDir     临时目录
     * @param command     执行命令
     * @param timeLimit   时间限制
     * @param memoryLimit 内存限制
     * @return 判题结果
     */
    private JudgeResult executeTestCasesInOrder(List<TestCase> testCases, Path tempDir,
            String[] command, long timeLimit, long memoryLimit) {

        List<CaseResult> allResults = new ArrayList<>();
        long totalExecutionTime = 0L;
        long maxMemoryUsed = 0L;

        // 顺序遍历所有测试用例 (LeetCode风格)
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);

            try {
                // 执行单个测试用例
                CaseResult result = executor.execute(testCase, tempDir, command, timeLimit, memoryLimit);

                // 记录性能数据
                performanceMonitor.recordExecution(
                        "case_" + i,
                        result.executionTime(),
                        result.memoryUsed() != null ? result.memoryUsed() : 0,
                        result.status() == SimpleResult.ACCEPTED);

                // 累计执行时间
                totalExecutionTime += result.executionTime();
                maxMemoryUsed = Math.max(maxMemoryUsed,
                        result.memoryUsed() != null ? result.memoryUsed() : 0);

                allResults.add(result);

                // LeetCode风格: 遇到失败立即返回
                if (!isAccepted(result.status())) {
                    log.info("Test case {} failed with status: {}, stopping execution", i, result.status());
                    return buildFailureResult(result, totalExecutionTime, maxMemoryUsed);
                }

            } catch (Exception e) {
                log.error("Error executing test case {}: {}", i, e.getMessage());
                CaseResult errorResult = buildErrorCaseResult(testCase, i, e);
                allResults.add(errorResult);
                return buildFailureResult(errorResult, totalExecutionTime, maxMemoryUsed);
            }
        }

        // 所有测试用例通过
        log.info("All {} test cases passed", testCases.size());
        return buildSuccessResult(totalExecutionTime, maxMemoryUsed, allResults);
    }

    /**
     * 编译用户代码
     *
     * @param compiler   编译器实例
     * @param sourceCode 源代码
     * @param tempDir    临时目录
     * @return 编译结果，如果成功返回null
     */
    private JudgeResult compileCode(Compiler compiler, String sourceCode, Path tempDir) {
        try {
            JudgeResult compileResult = compiler.compile(sourceCode, tempDir);

            // 检查编译是否失败
            if (compileResult != null && compileResult.status() != SimpleResult.ACCEPTED) {
                log.info("Compilation failed: {}", compileResult.message());
                return compileResult;
            }
        } catch (IOException e) {
            log.error("Compilation IO error: {}", e.getMessage());
            return buildErrorResult(SimpleResult.COMPILATION_ERROR,
                    "Compilation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Compilation error: {}", e.getMessage());
            return buildErrorResult(SimpleResult.COMPILATION_ERROR,
                    "Compilation error: " + e.getMessage());
        }
        return null;
    }

    /**
     * 判断是否为Accepted状态
     *
     * @param status 测试状态
     * @return 是否通过
     */
    private boolean isAccepted(SimpleResult status) {
        return status == SimpleResult.ACCEPTED;
    }

    /**
     * 构建成功结果 (所有测试用例通过)
     *
     * @param totalTime  总执行时间
     * @param maxMemory  最大内存使用
     * @param allResults 所有测试结果
     * @return 判题结果
     */
    private JudgeResult buildSuccessResult(long totalTime, long maxMemory, List<CaseResult> allResults) {
        return JudgeResult.builder()
                .status(SimpleResult.ACCEPTED)
                .message("All test cases passed")
                .executionTime(totalTime)
                .memoryUsed(maxMemory)
                .caseResults(null)
                .build();
    }

    /**
     * 构建失败结果
     *
     * LeetCode风格: 返回第一个失败的测试用例信息
     *
     * @param failedResult 失败的测试用例结果
     * @param totalTime    总执行时间
     * @param maxMemory    最大内存使用
     * @return 判题结果
     */
    private JudgeResult buildFailureResult(CaseResult failedResult, long totalTime, long maxMemory) {
        return JudgeResult.builder()
                .status(failedResult.status())
                .message(extractFailureMessage(failedResult))
                .executionTime(totalTime)
                .memoryUsed(maxMemory)
                .caseResults(failedResult)
                .build();
    }

    /**
     * 从测试结果中提取失败消息
     *
     * @param result 测试结果
     * @return 失败消息
     */
    private String extractFailureMessage(CaseResult result) {
        if (result == null) {
            return "Unknown error";
        }

        // 优先使用消息字段
        if (result.message() != null && !result.message().isEmpty()) {
            return result.message();
        }

        // 根据状态返回默认消息
        return switch (result.status()) {
            case WRONG_ANSWER -> "Wrong Answer";
            case TIME_LIMIT_EXCEEDED -> "Time Limit Exceeded";
            case MEMORY_LIMIT_EXCEEDED -> "Memory Limit Exceeded";
            case RUNTIME_ERROR -> "Runtime Error";
            default -> "Test case failed";
        };
    }

    /**
     * 构建错误测试结果
     */
    private CaseResult buildErrorCaseResult(TestCase testCase, int index, Exception e) {
        return CaseResult.builder()
                .status(SimpleResult.RUNTIME_ERROR)
                .message("Error in test case " + index + ": " + e.getMessage())
                .input(testCase.input())
                .expectedOutput(testCase.expectedOutput())
                .executionTime(0)
                .memoryUsed(0L)
                .build();
    }

    /**
     * 构建错误判题结果
     *
     * @param status  错误状态
     * @param message 错误消息
     * @return 判题结果
     */
    private JudgeResult buildErrorResult(SimpleResult status, String message) {
        return JudgeResult.builder()
                .status(status)
                .message(message)
                .executionTime(0)
                .memoryUsed(0)
                .build();
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
