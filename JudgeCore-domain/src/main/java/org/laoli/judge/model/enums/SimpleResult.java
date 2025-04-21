package org.laoli.judge.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *@description 标准错误枚举
 *@author laoli
 *@create 2025/4/19 12:51
 */
@Getter
@AllArgsConstructor
public enum SimpleResult {
    ACCEPTED("Accepted"),
    WRONG_ANSWER("Wrong Answer"),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded"),
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded"),
    RUNTIME_ERROR("Runtime Error"),
    COMPILATION_ERROR("Compilation Error"),
    SYSTEM_ERROR("System Error");

    private final String description;
}
