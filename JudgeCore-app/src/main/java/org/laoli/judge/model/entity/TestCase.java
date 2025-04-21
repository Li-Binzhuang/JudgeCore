package org.laoli.judge.model.entity;

import lombok.Builder;

/**
 * @author laoli
 * @description 测试用例数据类
 * @create 2025/4/20 11:57
 */
@Builder
public record TestCase(String input, String expectedOutput) {}