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
        if (caseResults.isEmpty()) {
            return JudgeResult.builder()
               .status(SimpleResult.SYSTEM_ERROR)
               .message("No test cases executed")
               .build();
        }

        if(caseResults.size()==1){
            return JudgeResult.builder()
                .status(caseResults.get(0).status())
                .message(caseResults.get(0).actualOutput())
                .executionTime(caseResults.get(0).executionTime())
                .memoryUsed(caseResults.get(0).memoryUsed())
                .caseResults(caseResults.get(0))
                .build();
        }

        // 计算总执行时间和最大内存使用
        long totalExecutionTime = caseResults.stream().mapToLong(CaseResult::executionTime).sum();
        double maxMemoryUsed = caseResults.stream().mapToDouble(CaseResult::memoryUsed).max().orElse(0);

        return JudgeResult.builder()
               .status(SimpleResult.ACCEPTED)
               .executionTime(totalExecutionTime)
               .memoryUsed(maxMemoryUsed)
               .build();
    }
}
