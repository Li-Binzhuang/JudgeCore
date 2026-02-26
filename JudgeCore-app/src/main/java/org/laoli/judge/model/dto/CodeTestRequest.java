package org.laoli.judge.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码测试请求DTO
 * 用于接收用户提交的代码片段进行测试
 * 支持有输入/输出的测试用例，也支持无输入仅执行代码的场景
 *
 * @author laoli
 * @date 2025/02/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeTestRequest {

    /**
     * 用户提交的源代码
     */
    @NotBlank(message = "代码不能为空")
    @Size(max = 65536, message = "代码长度不能超过65536字符")
    private String code;

    /**
     * 编程语言 (JAVA, PYTHON, CPP, C, RUST, GO, PHP)
     */
    @NotBlank(message = "语言不能为空")
    private String language;

    /**
     * 测试用例列表 (可选)
     * 如果为空，系统仅执行代码并返回执行结果
     * 如果不为空，系统会执行每个测试用例并验证结果
     */
    private List<CodeTestCase> testCases;

    /**
     * 时间限制 (毫秒)
     * 默认值: 1000ms
     * 最小值: 100ms
     * 最大值: 60000ms
     */
    @NotNull(message = "时间限制不能为空")
    @Builder.Default
    private Long timeLimit = 1000L;

    /**
     * 内存限制 (KB)
     * 默认值: 4096KB (4MB)
     * 最小值: 1024KB
     * 最大值: 512MB
     */
    @NotNull(message = "内存限制不能为空")
    @Builder.Default
    private Long memoryLimit = 4096L;

    /**
     * 是否显示详细输出
     * 默认为false，仅返回通过/失败状态
     * 设为true时，会返回每个测试用例的详细执行信息
     */
    @Builder.Default
    private Boolean showDetail = false;

    /**
     * 测试用例定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeTestCase {

        /**
         * 测试用例ID (可选)
         * 用于标识每个测试用例
         */
        private String id;

        /**
         * 测试输入 (可选)
         * 如果为空，表示该测试用例无输入，仅执行代码
         */
        private String input;

        /**
         * 预期输出 (可选)
         * 如果提供，将与实际输出进行比对
         * 如果为空，仅执行代码并返回执行结果
         */
        private String expectedOutput;

        /**
         * 测试用例描述 (可选)
         */
        private String description;
    }
}
