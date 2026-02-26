package org.laoli.judge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description HTTP判题响应DTO
 * @Author laoli
 * @Date 2025/4/20 15:58
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResponse {

    /**
     * 判题状态 (ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED, MEMORY_LIMIT_EXCEEDED,
     * RUNTIME_ERROR, COMPILATION_ERROR, SYSTEM_ERROR)
     */
    private String status;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 执行时间 (毫秒)
     */
    private Long executionTime;

    /**
     * 内存使用 (KB)
     */
    private Long memoryUsed;

    /**
     * 错误信息详情
     */
    private String errorDetail;

    /**
     * 测试用例信息
     */
    private CaseInfo caseInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseInfo {
        private String input;
        private String expectedOutput;
        private String actualOutput;
    }

    /**
     * 成功响应构建器
     */
    public static JudgeResponse success(String status, String message, Long executionTime,
            Long memoryUsed, CaseInfo caseInfo) {
        return JudgeResponse.builder()
                .status(status)
                .message(message)
                .executionTime(executionTime)
                .memoryUsed(memoryUsed)
                .caseInfo(caseInfo)
                .build();
    }

    /**
     * 错误响应构建器
     */
    public static JudgeResponse error(String status, String message, String errorDetail) {
        return JudgeResponse.builder()
                .status(status)
                .message(message)
                .errorDetail(errorDetail)
                .build();
    }
}
