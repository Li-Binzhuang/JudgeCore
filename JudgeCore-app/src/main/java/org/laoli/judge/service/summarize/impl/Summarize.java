package org.laoli.judge.service.summarize.impl;

import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.service.summarize.ISummarize;
import org.laoli.judge.model.enums.SimpleResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description 汇总测试结果
 * @Author laoli
 * @Date 2025/4/20 17:18
 */
@Component
public class Summarize implements ISummarize {
    @Override
    public JudgeResult summarizeResults(List<CaseResult> caseResults) {

        if (caseResults.size() == 1 && caseResults.get(0).status() != SimpleResult.ACCEPTED) {
            return JudgeResult.builder()
                    .status(caseResults.get(0).status())
                    .message(caseResults.get(0).message())
                    .executionTime(caseResults.get(0).executionTime())
                    .memoryUsed(caseResults.get(0).memoryUsed())
                    .caseResults(caseResults.get(0))
                    .build();
        }

        // 计算总执行时间和最大内存使用
        long totalExecutionTime = caseResults.stream().mapToLong(CaseResult::executionTime).sum();
        long maxMemoryUsed = caseResults.stream().mapToLong(CaseResult::memoryUsed).max().orElse(0);

        return JudgeResult.builder()
                .status(SimpleResult.ACCEPTED)
                .executionTime(totalExecutionTime)
                .memoryUsed(maxMemoryUsed)
                .build();
    }
}
