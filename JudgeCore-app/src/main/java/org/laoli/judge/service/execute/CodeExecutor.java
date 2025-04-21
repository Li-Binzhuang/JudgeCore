package org.laoli.judge.service.execute;


import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @Description 代码执行器接口
 * @Author laoli
 * @Date 2025/4/20 12:30
 */
public interface CodeExecutor {
    CaseResult execute(TestCase testCase, Path workDir, String[] command,long timeLimit,double memoryLimit) throws IOException, InterruptedException;
}
