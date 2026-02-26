package org.laoli.judge.controller;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.dto.JudgeRequest;
import org.laoli.judge.model.dto.JudgeResponse;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.service.IJudgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description HTTP判题控制器，提供RESTful接口
 * @Author laoli
 * @Date 2025/4/20 15:58
 */
@RestController
@RequestMapping("/api/judge")
@Slf4j
public class JudgeController {

    private final IJudgeService judgeService;

    public JudgeController(IJudgeService judgeService) {
        this.judgeService = judgeService;
    }

    /**
     * 判题接口
     *
     * @param request 判题请求
     * @return 判题结果
     */
    @PostMapping
    public ResponseEntity<JudgeResponse> judge(@RequestBody JudgeRequest request) {
        log.info("Received judge request - Language: {}, TimeLimit: {}ms, MemoryLimit: {}KB",
                request.getLanguage(), request.getTimeLimit(), request.getMemoryLimit());

        try {
            List<TestCase> testCases = request.getCases().stream()
                    .map(tc -> TestCase.builder()
                            .input(tc.getInput())
                            .expectedOutput(tc.getExpectedOutput())
                            .build())
                    .toList();

            Language language;
            try {
                language = Language.valueOf(request.getLanguage().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unsupported language: {}", request.getLanguage());
                return ResponseEntity.badRequest()
                        .body(JudgeResponse.error(
                                SimpleResult.SYSTEM_ERROR.name(),
                                "Unsupported language: " + request.getLanguage(),
                                "Supported languages: JAVA, PYTHON, CPP, C, RUST, GO, PHP"
                        ));
            }

            JudgeResult judgeResult = judgeService.judge(
                    testCases,
                    request.getCode(),
                    language,
                    request.getTimeLimit(),
                    request.getMemoryLimit()
            );

            return buildResponse(judgeResult);

        } catch (Exception e) {
            log.error("Error processing judge request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(JudgeResponse.error(
                            SimpleResult.SYSTEM_ERROR.name(),
                            "Internal server error",
                            e.getMessage()
                    ));
        }
    }

    /**
     * 健康检查接口
     *
     * @return 服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    /**
     * 获取支持的编程语言列表
     *
     * @return 支持的语言列表
     */
    @GetMapping("/languages")
    public ResponseEntity<List<String>> getSupportedLanguages() {
        List<String> languages = Language.getSupportLanguage();
        return ResponseEntity.ok(languages);
    }

    private ResponseEntity<JudgeResponse> buildResponse(JudgeResult judgeResult) {
        String status = judgeResult.status().name();
        String message = judgeResult.message();
        Long executionTime = judgeResult.executionTime();
        Double memoryUsed = judgeResult.memoryUsed();

        if (judgeResult.caseResults() != null) {
            JudgeResponse.CaseInfo caseInfo = JudgeResponse.CaseInfo.builder()
                    .input(judgeResult.caseResults().input())
                    .expectedOutput(judgeResult.caseResults().expectedOutput())
                    .actualOutput(judgeResult.caseResults().actualOutput())
                    .build();

            return ResponseEntity.ok(JudgeResponse.success(
                    status, message, executionTime, memoryUsed, caseInfo
            ));
        } else {
            return ResponseEntity.ok(JudgeResponse.success(
                    status, message, executionTime, memoryUsed, null
            ));
        }
    }
}
