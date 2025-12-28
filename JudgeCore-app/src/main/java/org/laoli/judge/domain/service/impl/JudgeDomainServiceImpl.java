package org.laoli.judge.domain.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.domain.service.IJudgeDomainService;
import org.laoli.judge.service.compile.Compiler;
import org.laoli.judge.service.compile.CompilerFactory;
import org.laoli.judge.service.execute.CodeExecutor;
import org.laoli.judge.service.execute.ExecutorFactory;
import org.laoli.judge.service.execute.LanguageCommandFactory;
import org.laoli.judge.service.summarize.ISummarize;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description 判题领域服务实现（领域层核心业务逻辑）
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@Service
public class JudgeDomainServiceImpl implements IJudgeDomainService {

    private final ThreadPoolExecutor executorService;
    private final ExecutorFactory executorFactory;
    private final LanguageCommandFactory languageCommandFactory;
    private final CompilerFactory compilerFactory;
    private final ISummarize summarize;

    public JudgeDomainServiceImpl(
            ThreadPoolExecutor executorService,
            ExecutorFactory executorFactory,
            LanguageCommandFactory languageCommandFactory,
            CompilerFactory compilerFactory,
            ISummarize summarize) {
        this.executorService = executorService;
        this.executorFactory = executorFactory;
        this.languageCommandFactory = languageCommandFactory;
        this.compilerFactory = compilerFactory;
        this.summarize = summarize;
    }

    /**
     * 主判题方法，处理一个完整的提交（领域核心业务逻辑）
     */
    @Override
    public JudgeResult judge(List<TestCase> testCases, String sourceCode, Language language, 
                            long timeLimit, double memoryLimit) {
        // 判空
        if (testCases == null || testCases.isEmpty() || sourceCode == null || sourceCode.isEmpty()) {
            return JudgeResult.builder()
                    .status(SimpleResult.SYSTEM_ERROR)
                    .message("Invalid input")
                    .build();
        }
        
        // 设置最小限制
        if (timeLimit < 1000 && memoryLimit < (1 << 22)) {
            timeLimit = 1000;
            memoryLimit = 1 << 22;
        }
        
        Path tempDir = null;
        Compiler compiler = compilerFactory.getCompiler(language);
        
        try {
            // 创建临时工作目录
            tempDir = Files.createTempDirectory("judge_");

            // 编译代码
            JudgeResult compileResult = compiler.compile(sourceCode, tempDir);

            // 如果编译出错，直接返回
            if (compileResult.status() == SimpleResult.COMPILATION_ERROR) {
                return compileResult;
            }

            // 并发执行所有测试用例
            List<CaseResult> caseResults = runTestsConcurrently(testCases, tempDir, language, timeLimit, memoryLimit);

            // 汇总结果
            return summarize.summarizeResults(caseResults);

        } catch (Exception e) {
            log.error("Error during judge: {}", e.getMessage(), e);
        } finally {
            // 清理临时目录
            cleanupTempDir(tempDir);
        }
        
        return JudgeResult.builder()
                .status(SimpleResult.SYSTEM_ERROR)
                .message("Judge process failed")
                .build();
    }

    private synchronized List<CaseResult> runTestsConcurrently(List<TestCase> testCases, Path tempDir, 
                                                               Language language, long timeLimit, double memoryLimit) {
        // 获取执行器
        CodeExecutor executor = executorFactory.getDefaultExecutor();
        
        // 并行执行所有测试用例
        String[] command = languageCommandFactory.getCommand(language, tempDir);
        List<Future<CaseResult>> futures = new ArrayList<>();
        
        testCases.forEach(testCase -> {
            futures.add(executorService.submit(() -> {
                try {
                    return executor.execute(testCase, tempDir, command, timeLimit, memoryLimit);
                } catch (IOException | InterruptedException e) {
                    log.error("Error executing test case: {}", e.getMessage());
                    return CaseResult.builder()
                            .status(SimpleResult.RUNTIME_ERROR)
                            .message(e.getMessage())
                            .input(testCase.input())
                            .expectedOutput(testCase.expectedOutput())
                            .build();
                }
            }));
        });

        List<CaseResult> caseResults = new ArrayList<>();
        for (Future<CaseResult> future : futures) {
            try {
                CaseResult caseResult = future.get();
                // 如果有一个测试用例执行失败，则直接返回
                if (caseResult.status() != SimpleResult.ACCEPTED) {
                    caseResults = null;
                    return Collections.singletonList(caseResult);
                }
                caseResults.add(caseResult);
            } catch (Exception e) {
                log.error("Error executing test case: {}", e.getMessage());
            }
        }
        return caseResults;
    }

    private void cleanupTempDir(Path tempDir) {
        if (tempDir != null) {
            try {
                // 递归删除目录
                Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.error("Error deleting file: {}", e.getMessage());
                        }
                    });
            } catch (IOException e) {
                log.error("Error deleting directory: {}", e.getMessage());
            }
        }
    }
}
