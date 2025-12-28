package org.laoli.judge.domain.service;

import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;

import java.util.List;

/**
 * @Description 判题领域服务接口（领域层）
 * @Author laoli
 * @Date 2025/4/21
 */
public interface IJudgeDomainService {
    /**
     * 执行判题（领域核心业务逻辑）
     * @param testCases 测试用例列表
     * @param sourceCode 源代码
     * @param language 编程语言
     * @param timeLimit 时间限制（毫秒）
     * @param memoryLimit 内存限制（KB）
     * @return 判题结果
     */
    JudgeResult judge(List<TestCase> testCases, String sourceCode, Language language, long timeLimit, double memoryLimit);
}
