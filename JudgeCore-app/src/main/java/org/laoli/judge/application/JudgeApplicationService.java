package org.laoli.judge.application;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.laoli.judge.interfaces.dto.JudgeResponse;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.domain.service.IJudgeDomainService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 判题应用服务（Application Service）
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@Service
public class JudgeApplicationService {
    
    private final IJudgeDomainService judgeDomainService;
    
    public JudgeApplicationService(IJudgeDomainService judgeDomainService) {
        this.judgeDomainService = judgeDomainService;
    }
    
    /**
     * 执行判题
     */
    public JudgeResponse judge(JudgeRequest request) {
        try {
            // 转换 DTO 到领域模型
            List<TestCase> testCases = request.getCases().stream()
                    .map(c -> TestCase.builder()
                            .input(c.getInput())
                            .expectedOutput(c.getExpectedOutput())
                            .build())
                    .collect(Collectors.toList());
            
            Language language = Language.valueOf(request.getLanguage().toUpperCase());
            
            // 调用领域服务
            JudgeResult judgeResult = judgeDomainService.judge(
                    testCases,
                    request.getCode(),
                    language,
                    request.getTimeLimit(),
                    request.getMemoryLimit()
            );
            
            // 转换领域模型到 DTO
            return convertToResponse(judgeResult);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid language: {}", request.getLanguage(), e);
            return JudgeResponse.builder()
                    .status("SYSTEM_ERROR")
                    .message("不支持的编程语言: " + request.getLanguage())
                    .build();
        } catch (Exception e) {
            log.error("Judge error", e);
            return JudgeResponse.builder()
                    .status("SYSTEM_ERROR")
                    .message("判题服务异常: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 转换领域模型到响应 DTO
     */
    private JudgeResponse convertToResponse(JudgeResult judgeResult) {
        JudgeResponse.JudgeResponseBuilder builder = JudgeResponse.builder()
                .status(judgeResult.status().toString())
                .message(judgeResult.message())
                .executionTime(judgeResult.executionTime())
                .memoryUsed(judgeResult.memoryUsed());
        
        if (judgeResult.caseResults() != null) {
            builder.caseInfo(JudgeResponse.CaseInfoDTO.builder()
                    .input(judgeResult.caseResults().input())
                    .expectedOutput(judgeResult.caseResults().expectedOutput())
                    .actualOutput(judgeResult.caseResults().actualOutput())
                    .build());
        }
        
        return builder.build();
    }
}
