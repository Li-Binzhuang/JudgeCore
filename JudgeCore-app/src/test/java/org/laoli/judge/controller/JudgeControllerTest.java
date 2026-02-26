package org.laoli.judge.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.dto.JudgeRequest;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.service.IJudgeService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @Description JudgeController单元测试
 * @Author laoli
 * @Date 2025/4/20 15:58
 */
@ExtendWith(MockitoExtension.class)
class JudgeControllerTest {

    @Mock
    private IJudgeService judgeService;

    @InjectMocks
    private JudgeController judgeController;

    @Test
    void testJudge_Success() {
        JudgeRequest request = new JudgeRequest();
        request.setCode("print('Hello')");
        request.setLanguage("PYTHON");
        request.setTimeLimit(1000L);
        request.setMemoryLimit(4194304.0);

        JudgeRequest.TestCaseDto testCaseDto = new JudgeRequest.TestCaseDto();
        testCaseDto.setInput("test");
        testCaseDto.setExpectedOutput("Hello");
        request.setCases(List.of(testCaseDto));

        CaseResult caseResult = CaseResult.builder()
                .input("test")
                .expectedOutput("Hello")
                .actualOutput("Hello")
                .status(SimpleResult.ACCEPTED)
                .build();

        JudgeResult judgeResult = JudgeResult.builder()
                .status(SimpleResult.ACCEPTED)
                .message("Accepted")
                .executionTime(100L)
                .memoryUsed(1024.0)
                .caseResults(caseResult)
                .build();

        when(judgeService.judge(anyList(), anyString(), eq(Language.PYTHON), anyLong(), anyDouble()))
                .thenReturn(judgeResult);

        ResponseEntity<org.laoli.judge.model.dto.JudgeResponse> response = judgeController.judge(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCEPTED", response.getBody().getStatus());
    }

    @Test
    void testJudge_UnsupportedLanguage() {
        JudgeRequest request = new JudgeRequest();
        request.setCode("print('Hello')");
        request.setLanguage("INVALID");
        request.setTimeLimit(1000L);
        request.setMemoryLimit(4194304.0);

        JudgeRequest.TestCaseDto testCaseDto = new JudgeRequest.TestCaseDto();
        testCaseDto.setInput("test");
        testCaseDto.setExpectedOutput("Hello");
        request.setCases(List.of(testCaseDto));

        ResponseEntity<org.laoli.judge.model.dto.JudgeResponse> response = judgeController.judge(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SYSTEM_ERROR", response.getBody().getStatus());
    }

    @Test
    void testJudge_CompilationError() {
        JudgeRequest request = new JudgeRequest();
        request.setCode("invalid code");
        request.setLanguage("PYTHON");
        request.setTimeLimit(1000L);
        request.setMemoryLimit(4194304.0);

        JudgeRequest.TestCaseDto testCaseDto = new JudgeRequest.TestCaseDto();
        testCaseDto.setInput("test");
        testCaseDto.setExpectedOutput("Hello");
        request.setCases(List.of(testCaseDto));

        JudgeResult judgeResult = JudgeResult.builder()
                .status(SimpleResult.COMPILATION_ERROR)
                .message("Compilation error")
                .executionTime(0L)
                .memoryUsed(0.0)
                .build();

        when(judgeService.judge(anyList(), anyString(), eq(Language.PYTHON), anyLong(), anyDouble()))
                .thenReturn(judgeResult);

        ResponseEntity<org.laoli.judge.model.dto.JudgeResponse> response = judgeController.judge(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("COMPILATION_ERROR", response.getBody().getStatus());
    }

    @Test
    void testHealth() {
        ResponseEntity<String> response = judgeController.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }

    @Test
    void testGetSupportedLanguages() {
        ResponseEntity<List<String>> response = judgeController.getSupportedLanguages();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("JAVA"));
        assertTrue(response.getBody().contains("PYTHON"));
        assertTrue(response.getBody().contains("CPP"));
    }
}
