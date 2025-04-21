package org.laoli.judge.service;

import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;

import java.util.List;

/**
 * @Description 判题服务接口
 * @Author laoli
 * @Date 2025/4/20 19:55
 */

public interface IJudgeService {
    JudgeResult judge(List<TestCase> testCases, String sourceCode, Language language, long timeLimit, double memoryLimit);
}
