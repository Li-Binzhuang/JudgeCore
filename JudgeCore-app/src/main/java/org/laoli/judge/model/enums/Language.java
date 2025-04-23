package org.laoli.judge.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description 语言支持枚举
 * @author laoli
 * @create 2025/4/20 12:05
 */

@AllArgsConstructor
@Getter
public enum Language {
    JAVA("JAVA"),
    PYTHON("PYTHON"),
    CPP("CPP"),
    C("C"),
    RUST("RUST"),
    GO("GO"),
    PHP("PHP");
    private final String Language;
}