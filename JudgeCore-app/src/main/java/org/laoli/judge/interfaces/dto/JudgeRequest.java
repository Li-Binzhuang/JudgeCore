package org.laoli.judge.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * @Description 判题请求 DTO
 * @Author laoli
 * @Date 2025/4/21
 */
@Data
public class JudgeRequest {
    
    @NotBlank(message = "代码不能为空")
    private String code;
    
    @NotBlank(message = "编程语言不能为空")
    private String language;
    
    @NotEmpty(message = "测试用例不能为空")
    private List<TestCaseDTO> cases;
    
    @NotNull(message = "时间限制不能为空")
    @Positive(message = "时间限制必须大于0")
    private Long timeLimit; // 毫秒
    
    @NotNull(message = "内存限制不能为空")
    @Positive(message = "内存限制必须大于0")
    private Double memoryLimit; // KB
    
    @Data
    public static class TestCaseDTO {
        @NotBlank(message = "输入不能为空")
        private String input;
        
        @NotBlank(message = "期望输出不能为空")
        private String expectedOutput;
    }
}
