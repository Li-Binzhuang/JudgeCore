package org.laoli.judge.model.entity;

import lombok.Builder;
import org.laoli.judge.model.enums.SimpleResult;

/**
 * @author laoli
 * @description 单个测试用例的执行结果
 * @create 2025/4/19 13:37
 */
@Builder
public record CaseResult(SimpleResult status, String message,long executionTime, double memoryUsed, String actualOutput,
                         String expectedOutput, String input) {
}