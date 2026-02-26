package org.laoli.judge.controller;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.dto.CodeTestRequest;
import org.laoli.judge.model.dto.CodeTestResponse;
import org.laoli.judge.model.dto.JudgeRequest;
import org.laoli.judge.model.dto.JudgeResponse;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.service.IJudgeService;
import org.laoli.judge.service.test.ICodeTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/judge")
@Slf4j
public class JudgeController {

    private final IJudgeService judgeService;
    private final ICodeTestService codeTestService;

    public JudgeController(IJudgeService judgeService, ICodeTestService codeTestService) {
        this.judgeService = judgeService;
        this.codeTestService = codeTestService;
    }

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
                                "Supported languages: JAVA, PYTHON, CPP, C, RUST, GO, PHP"));
            }

            JudgeResult judgeResult = judgeService.judge(
                    testCases,
                    request.getCode(),
                    language,
                    request.getTimeLimit(),
                    request.getMemoryLimit());

            return buildResponse(judgeResult);

        } catch (Exception e) {
            log.error("Error processing judge request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(JudgeResponse.error(
                            SimpleResult.SYSTEM_ERROR.name(),
                            "Internal server error",
                            e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/languages")
    public ResponseEntity<List<String>> getSupportedLanguages() {
        List<String> languages = Language.getSupportLanguage();
        return ResponseEntity.ok(languages);
    }

    @PostMapping("/test")
    public ResponseEntity<CodeTestResponse> executeTest(@RequestBody CodeTestRequest request) {
        log.info("Received code test request - Language: {}, TimeLimit: {}ms, MemoryLimit: {}KB, TestCases: {}",
                request.getLanguage(), request.getTimeLimit(), request.getMemoryLimit(),
                request.getTestCases() != null ? request.getTestCases().size() : 0);

        try {
            CodeTestResponse response = codeTestService.executeTest(request);
            log.info("Code test completed - Status: {}, Passed: {}/{}",
                    response.getStatus(), response.getPassedCount(), response.getTotalCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing code test request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(CodeTestResponse.buildError(
                            CodeTestResponse.TestStatus.SYSTEM_ERROR,
                            "Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/test/single")
    public ResponseEntity<CodeTestResponse> executeSingleTest(@RequestBody CodeTestRequest request) {
        log.info("Received single test request - Language: {}, TimeLimit: {}ms, MemoryLimit: {}KB",
                request.getLanguage(), request.getTimeLimit(), request.getMemoryLimit());

        try {
            CodeTestResponse response = codeTestService.executeSingleTest(request);
            log.info("Single test completed - Status: {}", response.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing single test request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(CodeTestResponse.buildError(
                            CodeTestResponse.TestStatus.SYSTEM_ERROR,
                            "Internal server error: " + e.getMessage()));
        }
    }

    private ResponseEntity<JudgeResponse> buildResponse(JudgeResult judgeResult) {
        String status = judgeResult.status().name();
        String message = judgeResult.message();
        Long executionTime = judgeResult.executionTime();
        Long memoryUsed = judgeResult.memoryUsed();

        if (judgeResult.caseResults() != null) {
            JudgeResponse.CaseInfo caseInfo = JudgeResponse.CaseInfo.builder()
                    .input(judgeResult.caseResults().input())
                    .expectedOutput(judgeResult.caseResults().expectedOutput())
                    .actualOutput(judgeResult.caseResults().actualOutput())
                    .build();

            return ResponseEntity.ok(JudgeResponse.success(
                    status, message, executionTime, memoryUsed, caseInfo));
        } else {
            return ResponseEntity.ok(JudgeResponse.success(
                    status, message, executionTime, memoryUsed, null));
        }
    }
}
