package org.laoli.judge.model.aggregate;

import lombok.Builder;
import org.laoli.judge.model.entity.*;
import org.laoli.judge.model.enums.SimpleResult;

import java.util.List;

/**
 * @author laoli
 * @description 所有测试用例的汇总结果
 * @create 2025/4/19 13:37
 */
@Builder
public record JudgeResult(SimpleResult status, String message, long executionTime, long memoryUsed,
                          CaseResult caseResults) {
}