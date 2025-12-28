package org.laoli.judge.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 判题响应 DTO
 * @Author laoli
 * @Date 2025/4/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResponse {
    
    private String status;
    
    private String message;
    
    private Long executionTime; // 毫秒
    
    private Double memoryUsed; // KB
    
    private CaseInfoDTO caseInfo;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseInfoDTO {
        private String input;
        private String expectedOutput;
        private String actualOutput;
    }
}
