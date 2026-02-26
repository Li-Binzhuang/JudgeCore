package org.laoli.judge.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotNull;
/**
 * @Description HTTP判题请求DTO
 * @Author laoli
 * @Date 2025/4/20 15:58
 */

@Data
public class JudgeRequest {

    /**
     * 源代码
     */
    @NotBlank(message = "代码不能为空")
    private String code;

    /**
     * 编程语言 (JAVA, PYTHON, CPP, C, RUST, GO, PHP)
     */
    @NotBlank(message = "语言不能为空")
    private String language;

    /**
     * 测试用例列表
     */
    @NotEmpty(message = "测试用例不能为空")
    private List<TestCaseDto> cases;

    /**
     * 时间限制 (毫秒)
     */
    @NotNull(message = "时间限制不能为空")
    private Long timeLimit;

    /**
     * 内存限制 (KB)
     */
    @NotNull(message = "内存限制不能为空")
    private Double memoryLimit;

    @Data
    public static class TestCaseDto {
        private String input;
        private String expectedOutput;
    }
}
