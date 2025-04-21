package org.laoli.judge.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @param timeLimit   毫秒
 * @param memoryLimit MB
 * @author laoli
 * @description 测试用例数据类
 * @create 2025/4/20 11:57
 */
public record TestCase(String input, String expectedOutput, long timeLimit, int memoryLimit) {}