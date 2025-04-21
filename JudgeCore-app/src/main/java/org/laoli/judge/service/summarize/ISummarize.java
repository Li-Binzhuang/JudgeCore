package org.laoli.judge.service.summarize;

import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.entity.CaseResult;

import java.util.List;

/**
 * @Description 汇总所有测试用例结果
 * @Author laoli
 * @Date 2025/4/20 17:17
 */
public interface ISummarize {
    JudgeResult summarizeResults(List<CaseResult> caseResults);
}
