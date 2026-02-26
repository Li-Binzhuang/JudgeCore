package org.laoli.judge.service.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InputValidator Tests")
class InputValidatorTest {

    private InputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InputValidator();
    }

    @Nested
    @DisplayName("Source Code Validation")
    class SourceCodeValidation {

        @Test
        @DisplayName("Should reject null source code")
        void shouldRejectNullSourceCode() {
            JudgeResult result = validator.validate(null, Language.JAVA, createTestCases(1), 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
            assertTrue(result.message().toLowerCase().contains("source code"));
        }

        @Test
        @DisplayName("Should reject empty source code")
        void shouldRejectEmptySourceCode() {
            JudgeResult result = validator.validate("", Language.JAVA, createTestCases(1), 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject blank source code")
        void shouldRejectBlankSourceCode(String blankCode) {
            JudgeResult result = validator.validate(blankCode, Language.JAVA, createTestCases(1), 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
        }

        @Test
        @DisplayName("Should reject oversized source code")
        void shouldRejectOversizedSourceCode() {
            String oversizedCode = "a".repeat(70000);
            JudgeResult result = validator.validate(oversizedCode, Language.JAVA, createTestCases(1), 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
            assertTrue(result.message().toLowerCase().contains("maximum length"));
        }

        @Test
        @DisplayName("Should accept valid source code")
        void shouldAcceptValidSourceCode() {
            String validCode = "public class Main { public static void main(String[] args) {} }";
            JudgeResult result = validator.validate(validCode, Language.JAVA, createTestCases(1), 1000L, 4096L);
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Language Validation")
    class LanguageValidation {

        @Test
        @DisplayName("Should reject null language")
        void shouldRejectNullLanguage() {
            String validCode = "public class Main { }";
            JudgeResult result = validator.validate(validCode, null, createTestCases(1), 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "JAVASCRIPT", "RUBY", ""})
        @DisplayName("Should reject unsupported language")
        void shouldRejectUnsupportedLanguage(String invalidLang) {
            String validCode = "public class Main { }";
            Language language = Language.valueOf(invalidLang);
            JudgeResult result = validator.validate(validCode, language, createTestCases(1), 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
        }
    }

    @Nested
    @DisplayName("Test Case Validation")
    class TestCaseValidation {

        @Test
        @DisplayName("Should reject null test cases")
        void shouldRejectNullTestCases() {
            String validCode = "public class Main { }";
            JudgeResult result = validator.validate(validCode, Language.JAVA, null, 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
            assertTrue(result.message().toLowerCase().contains("test case"));
        }

        @Test
        @DisplayName("Should reject empty test cases")
        void shouldRejectEmptyTestCases() {
            String validCode = "public class Main { }";
            JudgeResult result = validator.validate(validCode, Language.JAVA, new ArrayList<>(), 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
        }

        @Test
        @DisplayName("Should reject too many test cases")
        void shouldRejectTooManyTestCases() {
            String validCode = "public class Main { }";
            List<TestCase> tooManyCases = createTestCases(1001);
            JudgeResult result = validator.validate(validCode, Language.JAVA, tooManyCases, 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
            assertTrue(result.message().toLowerCase().contains("too many"));
        }

        @Test
        @DisplayName("Should reject null test case in list")
        void shouldRejectNullTestCaseInList() {
            String validCode = "public class Main { }";
            List<TestCase> casesWithNull = new ArrayList<>();
            casesWithNull.add(null);
            JudgeResult result = validator.validate(validCode, Language.JAVA, casesWithNull, 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
            assertTrue(result.message().contains("null"));
        }

        @Test
        @DisplayName("Should reject oversized test case input")
        void shouldRejectOversizedTestCaseInput() {
            String validCode = "public class Main { }";
            String oversizedInput = "x".repeat(102401);
            List<TestCase> oversizedCases = List.of(
                    TestCase.builder()
                            .input(oversizedInput)
                            .expectedOutput("output")
                            .build()
            );
            JudgeResult result = validator.validate(validCode, Language.JAVA, oversizedCases, 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
            assertTrue(result.message().toLowerCase().contains("exceeds"));
        }

        @Test
        @DisplayName("Should accept valid test cases")
        void shouldAcceptValidTestCases() {
            String validCode = "public class Main { }";
            List<TestCase> validCases = List.of(
                    TestCase.builder().input("1 2").expectedOutput("3").build(),
                    TestCase.builder().input("3 4").expectedOutput("7").build()
            );
            JudgeResult result = validator.validate(validCode, Language.JAVA, validCases, 1000L, 4096L);
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Time and Memory Limit Validation")
    class LimitValidation {

        @ParameterizedTest
        @ValueSource(longs = {0, 50, 99})
        @DisplayName("Should reject time limit below minimum")
        void shouldRejectTimeLimitBelowMinimum(long timeLimit) {
            String validCode = "public class Main { }";
            JudgeResult result = validator.validate(validCode, Language.JAVA, createTestCases(1), timeLimit, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
            assertTrue(result.message().toLowerCase().contains("time limit"));
        }

        @ParameterizedTest
        @ValueSource(longs = {60001, 100000, 1000000})
        @DisplayName("Should reject time limit above maximum")
        void shouldRejectTimeLimitAboveMaximum(long timeLimit) {
            String validCode = "public class Main { }";
            JudgeResult result = validator.validate(validCode, Language.JAVA, createTestCases(1), timeLimit, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
            assertTrue(result.message().toLowerCase().contains("time limit"));
        }

        @ParameterizedTest
        @ValueSource(longs = {0, 512, 1023})
        @DisplayName("Should reject memory limit below minimum")
        void shouldRejectMemoryLimitBelowMinimum(long memoryLimit) {
            String validCode = "public class Main { }";
            JudgeResult result = validator.validate(validCode, Language.JAVA, createTestCases(1), 1000L, memoryLimit);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
            assertTrue(result.message().toLowerCase().contains("memory limit"));
        }

        @ParameterizedTest
        @ValueSource(longs = {536870913L, 1000000000L})
        @DisplayName("Should reject memory limit above maximum")
        void shouldRejectMemoryLimitAboveMaximum(long memoryLimit) {
            String validCode = "public class Main { }";
            JudgeResult result = validator.validate(validCode, Language.JAVA, createTestCases(1), 1000L, memoryLimit);
            assertNotNull(result);
            assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
            assertTrue(result.message().toLowerCase().contains("memory limit"));
        }

        @Test
        @DisplayName("Should accept valid limits")
        void shouldAcceptValidLimits() {
            String validCode = "public class Main { }";
            JudgeResult result = validator.validate(validCode, Language.JAVA, createTestCases(1), 5000L, 65536L);
            assertNull(result);
        }

        @Test
        @DisplayName("Should apply default limits when too low")
        void shouldApplyDefaultLimitsWhenTooLow() {
            Long[] timeLimit = {50L};
            Long[] memoryLimit = {512L};
            validator.applyDefaultLimits(timeLimit, memoryLimit);
            assertTrue(timeLimit[0] >= 100);
            assertTrue(memoryLimit[0] >= 1024);
        }
    }

    @Nested
    @DisplayName("Security Validation")
    class SecurityValidation {

        @Test
        @DisplayName("Should reject dangerous Java code")
        void shouldRejectDangerousJavaCode() {
            String dangerousCode = "Runtime.getRuntime().exec(\"ls\");";
            JudgeResult result = validator.validate(dangerousCode, Language.JAVA, createTestCases(1), 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.RUNTIME_ERROR, result.status());
            assertTrue(result.message().toLowerCase().contains("dangerous"));
        }

        @Test
        @DisplayName("Should reject System.exit call")
        void shouldRejectSystemExitCall() {
            String dangerousCode = "System.exit(0);";
            JudgeResult result = validator.validate(dangerousCode, Language.JAVA, createTestCases(1), 1000L, 4096L);
            assertNotNull(result);
            assertEquals(SimpleResult.RUNTIME_ERROR, result.status());
        }
    }

    private List<TestCase> createTestCases(int count) {
        List<TestCase> cases = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cases.add(TestCase.builder()
                    .input("input" + i)
                    .expectedOutput("output" + i)
                    .build());
        }
        return cases;
    }
}
