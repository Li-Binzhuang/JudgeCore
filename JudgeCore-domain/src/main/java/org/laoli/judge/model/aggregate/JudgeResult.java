package org.laoli.judge.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.enums.SimpleResult;

/**
 * @author laoli
 * @description 所有测试用例的汇总结果
 * @create 2025/4/19 13:37
 */
@Builder
public record JudgeResult(SimpleResult status, String message, long executionTime, double memoryUsed,
                          CaseResult caseResults) {
}