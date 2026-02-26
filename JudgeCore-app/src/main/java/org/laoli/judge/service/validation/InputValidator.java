package org.laoli.judge.service.validation;

import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class InputValidator {

    private static final long MIN_TIME_LIMIT = 100;
    private static final long MAX_TIME_LIMIT = 60000;
    private static final long MIN_MEMORY_LIMIT = 1024;
    private static final long MAX_MEMORY_LIMIT = 536870912;
    private static final int MAX_CODE_LENGTH = 65536;
    private static final int MAX_TEST_CASE_COUNT = 1000;
    private static final int MAX_TEST_CASE_SIZE = 102400;

    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
            ".*(Runtime\\.getRuntime\\(\\)|ProcessBuilder|ProcessImpl|System\\.exit|exec\\(|loadLibrary|class\\.forName|"
                    + "reflect\\.|Unsafe\\.|FileInputStream|FileOutputStream|RandomAccessFile|ServerSocket|Socket|"
                    + "URLClassLoader|\\.\\.\\/|\\.\\.\\\\).*",
            Pattern.CASE_INSENSITIVE);

    public JudgeResult validate(String sourceCode, Language language, List<TestCase> testCases,
            Long timeLimit, Long memoryLimit) {

        if (sourceCode == null || sourceCode.isEmpty()) {
            return buildErrorResult(SimpleResult.SYSTEM_ERROR, "Source code cannot be empty");
        }

        if (sourceCode.length() > MAX_CODE_LENGTH) {
            return buildErrorResult(SimpleResult.SYSTEM_ERROR,
                    "Source code exceeds maximum length of " + MAX_CODE_LENGTH + " characters");
        }

        if (!isValidLanguage(language)) {
            return buildErrorResult(SimpleResult.SYSTEM_ERROR, "Unsupported language: " + language);
        }

        if (testCases == null || testCases.isEmpty()) {
            return buildErrorResult(SimpleResult.SYSTEM_ERROR, "Test cases cannot be empty");
        }

        if (testCases.size() > MAX_TEST_CASE_COUNT) {
            return buildErrorResult(SimpleResult.SYSTEM_ERROR,
                    "Too many test cases, maximum is " + MAX_TEST_CASE_COUNT);
        }

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            if (testCase == null) {
                return buildErrorResult(SimpleResult.SYSTEM_ERROR,
                        "Test case at index " + i + " is null");
            }

            if (testCase.input() != null && testCase.input().length() > MAX_TEST_CASE_SIZE) {
                return buildErrorResult(SimpleResult.SYSTEM_ERROR,
                        "Test case input at index " + i + " exceeds maximum size of " + MAX_TEST_CASE_SIZE);
            }

            if (testCase.expectedOutput() != null && testCase.expectedOutput().length() > MAX_TEST_CASE_SIZE) {
                return buildErrorResult(SimpleResult.SYSTEM_ERROR,
                        "Test case expected output at index " + i + " exceeds maximum size of " + MAX_TEST_CASE_SIZE);
            }
        }

        if (timeLimit == null || memoryLimit == null) {
            return buildErrorResult(SimpleResult.SYSTEM_ERROR, "Time limit and memory limit cannot be null");
        }

        if (timeLimit < MIN_TIME_LIMIT || timeLimit > MAX_TIME_LIMIT) {
            return buildErrorResult(SimpleResult.SYSTEM_ERROR,
                    "Time limit must be between " + MIN_TIME_LIMIT + "ms and " + MAX_TIME_LIMIT + "ms");
        }

        if (memoryLimit < MIN_MEMORY_LIMIT || memoryLimit > MAX_MEMORY_LIMIT) {
            return buildErrorResult(SimpleResult.SYSTEM_ERROR,
                    "Memory limit must be between " + MIN_MEMORY_LIMIT + "KB and " + MAX_MEMORY_LIMIT + "KB");
        }

        if (containsDangerousCode(sourceCode, language)) {
            return buildErrorResult(SimpleResult.RUNTIME_ERROR,
                    "Code contains potentially dangerous operations");
        }

        return null;
    }

    public void applyDefaultLimits(Long[] timeLimit, Long[] memoryLimit) {
        if (timeLimit[0] < MIN_TIME_LIMIT) {
            timeLimit[0] = MIN_TIME_LIMIT;
        }
        if (memoryLimit[0] < MIN_MEMORY_LIMIT) {
            memoryLimit[0] = MIN_MEMORY_LIMIT;
        }
    }

    private boolean isValidLanguage(Language language) {
        return language != null&&Language.getSupportLanguage().contains(language.getLanguage());
    }

    private boolean containsDangerousCode(String sourceCode, Language language) {
        if (sourceCode == null) {
            return false;
        }

        if (DANGEROUS_PATTERN.matcher(sourceCode).matches()) {
            return true;
        }

        if (language == Language.JAVA) {
            return sourceCode.contains("Runtime.getRuntime()")
                    || sourceCode.contains("System.exit(")
                    || sourceCode.contains("ProcessBuilder");
        }

        return false;
    }

    private JudgeResult buildErrorResult(SimpleResult status, String message) {
        return JudgeResult.builder()
                .status(status)
                .message(message)
                .executionTime(0)
                .memoryUsed(0)
                .build();
    }
}
