package org.laoli.judge.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.service.IJudgeService;
import org.laoli.judge.service.compile.Compiler;
import org.laoli.judge.service.compile.CompilerFactory;
import org.laoli.judge.service.execute.CodeExecutor;
import org.laoli.judge.service.execute.LanguageCommandFactory;
import org.laoli.judge.service.summarize.ISummarize;
import org.laoli.judge.model.enums.SimpleResult;
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
 * @Description 判题服务核心实现
 * @Author laoli
 * @Date 2025/4/20 15:58
 */

@Slf4j
@Service
public class JudgeService implements IJudgeService {

    @Resource
    ThreadPoolExecutor executorService;
    @Resource
    private CodeExecutor executor;
    @Resource
    private LanguageCommandFactory languageCommandFactory;
    @Resource
    private CompilerFactory compilerFactory;
    @Resource
    private ISummarize summarize;
    /**
     * 主判题方法，处理一个完整的提交
     */
    @Override
    public JudgeResult judge(List<TestCase> testCases, String sourceCode, Language language,long timeLimit,double memoryLimit) {
        // 判空
        if (testCases == null || testCases.isEmpty() || sourceCode == null || sourceCode.isEmpty()) {
            return JudgeResult.builder()
                    .status(SimpleResult.SYSTEM_ERROR)
                    .message("Invalid input")
                    .build();
        }
        if(timeLimit<2000 && memoryLimit<(1<<23)){
            timeLimit=2000;
            memoryLimit=1<<23;
        }
        Path tempDir = null;
        //使用compiler工厂获取对应语言的编译器
        Compiler compiler = compilerFactory.getCompiler(language);
        try {
            // 创建临时工作目录
            tempDir = Files.createTempDirectory("judge_");

            // 编译代码
            JudgeResult compileResult = compiler.compile(sourceCode, tempDir);

            // 如果编译出错，直接返回
            if (compileResult.status()==SimpleResult.COMPILATION_ERROR) {
                return compileResult;
            }

            //并发执行所有测试用例
            List<CaseResult> caseResults = runTestsConcurrently(testCases, tempDir, language,timeLimit,memoryLimit);

            // 汇总结果
            return summarize.summarizeResults(caseResults);

        } catch (Exception e) {
            log.error("Error during judge: {}", e.getMessage());
        } finally {
            // 清理临时目录
            cleanupTempDir(tempDir);
        }
        return JudgeResult.builder()
                .status(SimpleResult.SYSTEM_ERROR).build();
    }

    private synchronized List<CaseResult> runTestsConcurrently(List<TestCase> testCases, Path tempDir, Language language,long timeLimit,double memoryLimit) {
        // 并行执行所有测试用例
        String[] command = languageCommandFactory.getCommand(language, tempDir);
        List<Future<CaseResult>> futures = new ArrayList<>();
        testCases.forEach(testCase -> {
            futures.add(executorService.submit(() -> executor.execute(testCase, tempDir,command,timeLimit,memoryLimit)));
        });
        List<CaseResult> caseResults = new ArrayList<>();
        for (Future<CaseResult> future : futures) {
            try {
                CaseResult caseResult = future.get();
                //如果有一个测试用例执行失败，则直接返回,后续需要优化
                if(caseResult.status()!=SimpleResult.ACCEPTED){
                    caseResults=null;//清空caseResults
                    return Collections.singletonList(caseResult);
                }
                caseResults.add(future.get());
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
