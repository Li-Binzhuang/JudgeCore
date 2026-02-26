package org.laoli.judge.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private final String language;

    public static List<String> getSupportLanguage() {
        return Arrays.stream(Language.values())
                .map(Language::getLanguage)
                .collect(Collectors.toList());
    }
}