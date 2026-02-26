package org.laoli.judge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码测试响应DTO
 * 返回代码执行和测试验证的结果
 *
 * @author laoli
 * @date 2025/02/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeTestResponse {

    /**
     * 执行状态
     */
    private TestStatus status;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 通过的测试用例数量
     */
    private Integer passedCount;

    /**
     * 总测试用例数量
     */
    private Integer totalCount;

    /**
     * 所有测试用例的结果列表
     */
    private List<CaseTestResult> caseResults;

    /**
     * 总执行时间 (毫秒)
     */
    private Long totalExecutionTime;

    /**
     * 最大内存使用 (KB)
     */
    private Long maxMemoryUsed;

    /**
     * 测试用例执行结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseTestResult {

        /**
         * 测试用例ID
         */
        private String caseId;

        /**
         * 测试用例索引
         */
        private Integer index;

        /**
         * 测试用例描述
         */
        private String description;

        /**
         * 测试输入
         */
        private String input;

        /**
         * 预期输出
         */
        private String expectedOutput;

        /**
         * 实际输出
         */
        private String actualOutput;

        /**
         * 测试结果状态
         */
        private TestStatus status;

        /**
         * 执行时间 (毫秒)
         */
        private Long executionTime;

        /**
         * 内存使用 (KB)
         */
        private Long memoryUsed;

        /**
         * 错误信息 (如果有)
         */
        private String errorMessage;
    }

    /**
     * 测试状态枚举
     */
    public enum TestStatus {
        /**
         * 所有测试通过
         */
        ALL_PASSED,

        /**
         * 部分测试通过
         */
        PARTIAL_PASSED,

        /**
         * 所有测试失败
         */
        ALL_FAILED,

        /**
         * 编译错误
         */
        COMPILE_ERROR,

        /**
         * 执行超时
         */
        TIME_LIMIT_EXCEEDED,

        /**
         * 内存超限
         */
        MEMORY_LIMIT_EXCEEDED,

        /**
         * 运行时错误
         */
        RUNTIME_ERROR,

        /**
         * 系统错误
         */
        SYSTEM_ERROR,

        /**
         * 无需测试 (仅执行代码)
         */
        EXECUTED_ONLY
    }

    /**
     * 构建成功响应 (仅执行，无测试用例)
     */
    public static CodeTestResponse buildExecutedOnly(Long executionTime, Long memoryUsed, String output) {
        return CodeTestResponse.builder()
                .status(TestStatus.EXECUTED_ONLY)
                .message("Code executed successfully")
                .passedCount(0)
                .totalCount(0)
                .caseResults(null)
                .totalExecutionTime(executionTime)
                .maxMemoryUsed(memoryUsed)
                .build();
    }

    /**
         * 构建成功响应 (所有测试通过)
         */
    public static CodeTestResponse buildAllPassed(List<CaseTestResult> results, Long totalTime, Long maxMemory) {
        return CodeTestResponse.builder()
                .status(TestStatus.ALL_PASSED)
                .message("All test cases passed")
                .passedCount(results.size())
                .totalCount(results.size())
                .caseResults(results)
                .totalExecutionTime(totalTime)
                .maxMemoryUsed(maxMemory)
                .build();
    }

    /**
     * 构建部分通过响应
     */
    public static CodeTestResponse buildPartialPassed(List<CaseTestResult> results, Long totalTime, Long maxMemory) {
        long passed = results.stream().filter(r -> r.getStatus() == TestStatus.ALL_PASSED).count();
        return CodeTestResponse.builder()
                .status(TestStatus.PARTIAL_PASSED)
                .message(String.format("%d/%d test cases passed", passed, results.size()))
                .passedCount((int) passed)
                .totalCount(results.size())
                .caseResults(results)
                .totalExecutionTime(totalTime)
                .maxMemoryUsed(maxMemory)
                .build();
    }

    /**
         * 构建失败响应
         */
    public static CodeTestResponse buildFailed(List<CaseTestResult> results, Long totalTime, Long maxMemory) {
        return CodeTestResponse.builder()
                .status(TestStatus.ALL_FAILED)
                .message("All test cases failed")
                .passedCount(0)
                .totalCount(results.size())
                .caseResults(results)
                .totalExecutionTime(totalTime)
                .maxMemoryUsed(maxMemory)
                .build();
    }

    /**
     * 构建错误响应
     */
    public static CodeTestResponse buildError(TestStatus status, String message) {
        return CodeTestResponse.builder()
                .status(status)
                .message(message)
                .passedCount(0)
                .totalCount(0)
                .caseResults(null)
                .totalExecutionTime(0L)
                .maxMemoryUsed(0L)
                .build();
    }
}
