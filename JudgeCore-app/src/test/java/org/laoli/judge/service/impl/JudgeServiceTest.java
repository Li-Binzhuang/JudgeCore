package org.laoli.judge.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.service.compile.Compiler;
import org.laoli.judge.service.compile.CompilerFactory;
import org.laoli.judge.service.execute.CodeExecutor;
import org.laoli.judge.service.execute.LanguageCommandFactory;
import org.laoli.judge.service.summarize.ISummarize;
import org.laoli.judge.service.comparator.OutputComparator;
import org.laoli.judge.service.validation.InputValidator;
import org.laoli.judge.service.monitor.PerformanceMonitor;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JudgeService Tests")
public class JudgeServiceTest {

        @Mock
        private ThreadPoolExecutor executorService;

        @Mock
        private CodeExecutor executor;

        @Mock
        private LanguageCommandFactory languageCommandFactory;

        @Mock
        private CompilerFactory compilerFactory;

        @Mock
        private ISummarize summarize;

        @Mock
        private InputValidator inputValidator;

        @Mock
        private OutputComparator outputComparator;

        @Mock
        private PerformanceMonitor performanceMonitor;

        @InjectMocks
        private JudgeService judgeService;

        private static final String VALID_CODE = "public class Main { public static void main(String[] args) { } }";
        private static final Language JAVA = Language.JAVA;
        private static final long DEFAULT_TIME_LIMIT = 1000L;
        private static final long DEFAULT_MEMORY_LIMIT = 4096L;

        @BeforeEach
        void setUp() {
                doAnswer(invocation -> {
                        Long[] time = invocation.getArgument(0);
                        Long[] memory = invocation.getArgument(1);
                        if (time[0] < 100)
                                time[0] = 100L;
                        if (memory[0] < 1024)
                                memory[0] = 1024L;
                        return null;
                }).when(inputValidator).applyDefaultLimits(any(Long[].class), any(Long[].class));
        }

        @Nested
        @DisplayName("Normal Input Scenarios")
        class NormalInputTests {

                @Test
                @DisplayName("Should return ACCEPTED when all test cases pass")
                void shouldReturnAcceptedWhenAllTestCasesPass() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("1 2").expectedOutput("3").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any(Path.class)))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult acceptedResult = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .build();

                        when(executor.execute(any(), any(), any(), anyLong(), anyLong()))
                                        .thenReturn(acceptedResult);

                        JudgeResult summaryResult = JudgeResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .message("All test cases passed")
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .build();
                        when(summarize.summarizeResults(anyList())).thenReturn(summaryResult);

                        when(performanceMonitor.getSummary())
                                        .thenReturn(new PerformanceMonitor.PerformanceSummary(50L, 1024L, 1, 0));

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.ACCEPTED, result.status());
                }

                @Test
                @DisplayName("Should handle multiple test cases")
                void shouldHandleMultipleTestCases() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("1 2").expectedOutput("3").build(),
                                        TestCase.builder().input("2 3").expectedOutput("5").build(),
                                        TestCase.builder().input("5 5").expectedOutput("10").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any(Path.class)))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult acceptedResult = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .build();

                        when(executor.execute(any(), any(), any(), anyLong(), anyLong()))
                                        .thenReturn(acceptedResult);

                        JudgeResult summaryResult = JudgeResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(150L)
                                        .memoryUsed(1024L)
                                        .build();
                        when(summarize.summarizeResults(anyList())).thenReturn(summaryResult);

                        when(performanceMonitor.getSummary())
                                        .thenReturn(new PerformanceMonitor.PerformanceSummary(150L, 1024L, 3, 0));

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.ACCEPTED, result.status());
                }
        }

        @Nested
        @DisplayName("Boundary Condition Tests")
        class BoundaryConditionTests {

                @Test
                @DisplayName("Should handle minimum time limit")
                void shouldHandleMinimumTimeLimit() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("test").expectedOutput("test").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), eq(100L), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any(Path.class)))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult acceptedResult = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .build();

                        when(executor.execute(any(), any(), any(), eq(100L), anyLong()))
                                        .thenReturn(acceptedResult);

                        JudgeResult summaryResult = JudgeResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .build();
                        when(summarize.summarizeResults(anyList())).thenReturn(summaryResult);

                        when(performanceMonitor.getSummary())
                                        .thenReturn(new PerformanceMonitor.PerformanceSummary(50L, 1024L, 1, 0));

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, 50L, DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                }

                @Test
                @DisplayName("Should handle single test case")
                void shouldHandleSingleTestCase() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("1").expectedOutput("1").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any(Path.class)))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult acceptedResult = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(10L)
                                        .memoryUsed(512L)
                                        .build();

                        when(executor.execute(any(), any(), any(), anyLong(), anyLong()))
                                        .thenReturn(acceptedResult);

                        JudgeResult summaryResult = JudgeResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(10L)
                                        .memoryUsed(512L)
                                        .build();
                        when(summarize.summarizeResults(anyList())).thenReturn(summaryResult);

                        when(performanceMonitor.getSummary())
                                        .thenReturn(new PerformanceMonitor.PerformanceSummary(10L, 512L, 1, 0));

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.ACCEPTED, result.status());
                }

                @Test
                @DisplayName("Should handle empty input in test case")
                void shouldHandleEmptyInputInTestCase() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("").expectedOutput("").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any(Path.class)))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult acceptedResult = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(10L)
                                        .memoryUsed(512L)
                                        .build();

                        when(executor.execute(any(), any(), any(), anyLong(), anyLong()))
                                        .thenReturn(acceptedResult);

                        JudgeResult summaryResult = JudgeResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(10L)
                                        .memoryUsed(512L)
                                        .build();
                        when(summarize.summarizeResults(anyList())).thenReturn(summaryResult);

                        when(performanceMonitor.getSummary())
                                        .thenReturn(new PerformanceMonitor.PerformanceSummary(10L, 512L, 1, 0));

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                }
        }

        @Nested
        @DisplayName("Exception Input Tests")
        class ExceptionInputTests {

                @Test
                @DisplayName("Should return SYSTEM_ERROR for invalid source code")
                void shouldReturnSystemErrorForInvalidSourceCode() {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("test").expectedOutput("test").build());

                        JudgeResult validationError = JudgeResult.builder()
                                        .status(SimpleResult.SYSTEM_ERROR)
                                        .message("Source code cannot be empty")
                                        .build();

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(validationError);

                        JudgeResult result = judgeService.judge(testCases, "", JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
                        assertEquals("Source code cannot be empty", result.message());
                }

                @Test
                @DisplayName("Should return COMPILATION_ERROR when compilation fails")
                void shouldReturnCompilationErrorWhenCompilationFails() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("test").expectedOutput("test").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        Compiler mockCompiler = mock(Compiler.class);
                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mockCompiler);

                        JudgeResult compilationError = JudgeResult.builder()
                                        .status(SimpleResult.COMPILATION_ERROR)
                                        .message("Compilation failed: syntax error")
                                        .build();
                        when(mockCompiler.compile(anyString(), any(Path.class))).thenReturn(compilationError);

                        JudgeResult result = judgeService.judge(testCases, "invalid code", JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.COMPILATION_ERROR, result.status());
                }

                @Test
                @DisplayName("Should return WRONG_ANSWER when output doesn't match")
                void shouldReturnWrongAnswerWhenOutputMismatch() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("1 2").expectedOutput("3").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any(Path.class)))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult wrongAnswerResult = CaseResult.builder()
                                        .status(SimpleResult.WRONG_ANSWER)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .expectedOutput("3")
                                        .actualOutput("5")
                                        .build();

                        when(executor.execute(any(), any(), any(), anyLong(), anyLong()))
                                        .thenReturn(wrongAnswerResult);

                        JudgeResult summaryResult = JudgeResult.builder()
                                        .status(SimpleResult.WRONG_ANSWER)
                                        .message("Wrong Answer")
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .build();
                        when(summarize.summarizeResults(anyList())).thenReturn(summaryResult);

                        when(performanceMonitor.getSummary())
                                        .thenReturn(new PerformanceMonitor.PerformanceSummary(50L, 1024L, 0, 1));

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.WRONG_ANSWER, result.status());
                }

                @Test
                @DisplayName("Should return TIME_LIMIT_EXCEEDED when execution times out")
                void shouldReturnTimeLimitExceededWhenExecutionTimesOut() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("test").expectedOutput("test").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any(Path.class)))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult timeoutResult = CaseResult.builder()
                                        .status(SimpleResult.TIME_LIMIT_EXCEEDED)
                                        .executionTime(DEFAULT_TIME_LIMIT + 100)
                                        .memoryUsed(1024L)
                                        .build();

                        when(executor.execute(any(), any(), any(), anyLong(), anyLong()))
                                        .thenReturn(timeoutResult);

                        JudgeResult summaryResult = JudgeResult.builder()
                                        .status(SimpleResult.TIME_LIMIT_EXCEEDED)
                                        .message("Time Limit Exceeded")
                                        .executionTime(DEFAULT_TIME_LIMIT + 100)
                                        .memoryUsed(1024L)
                                        .build();
                        when(summarize.summarizeResults(anyList())).thenReturn(summaryResult);

                        when(performanceMonitor.getSummary())
                                        .thenReturn(new PerformanceMonitor.PerformanceSummary(DEFAULT_TIME_LIMIT + 100,
                                                        1024L, 0, 1));

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.TIME_LIMIT_EXCEEDED, result.status());
                }

                @Test
                @DisplayName("Should return MEMORY_LIMIT_EXCEEDED when memory limit exceeded")
                void shouldReturnMemoryLimitExceededWhenMemoryLimitExceeded() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("test").expectedOutput("test").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any(Path.class)))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult memoryLimitResult = CaseResult.builder()
                                        .status(SimpleResult.MEMORY_LIMIT_EXCEEDED)
                                        .executionTime(50L)
                                        .memoryUsed(DEFAULT_MEMORY_LIMIT + 1024)
                                        .build();

                        when(executor.execute(any(), any(), any(), anyLong(), anyLong()))
                                        .thenReturn(memoryLimitResult);

                        JudgeResult summaryResult = JudgeResult.builder()
                                        .status(SimpleResult.MEMORY_LIMIT_EXCEEDED)
                                        .message("Memory Limit Exceeded")
                                        .executionTime(50L)
                                        .memoryUsed(DEFAULT_MEMORY_LIMIT + 1024)
                                        .build();
                        when(summarize.summarizeResults(anyList())).thenReturn(summaryResult);

                        when(performanceMonitor.getSummary())
                                        .thenReturn(new PerformanceMonitor.PerformanceSummary(50L,
                                                        DEFAULT_MEMORY_LIMIT + 1024, 0, 1));

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.MEMORY_LIMIT_EXCEEDED, result.status());
                }

                @Test
                @DisplayName("Should return RUNTIME_ERROR when runtime exception occurs")
                void shouldReturnRuntimeErrorWhenRuntimeExceptionOccurs() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("test").expectedOutput("test").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any(Path.class)))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult runtimeErrorResult = CaseResult.builder()
                                        .status(SimpleResult.RUNTIME_ERROR)
                                        .message("NullPointerException")
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .build();

                        when(executor.execute(any(), any(), any(), anyLong(), anyLong()))
                                        .thenReturn(runtimeErrorResult);

                        JudgeResult summaryResult = JudgeResult.builder()
                                        .status(SimpleResult.RUNTIME_ERROR)
                                        .message("Runtime Error")
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .build();
                        when(summarize.summarizeResults(anyList())).thenReturn(summaryResult);

                        when(performanceMonitor.getSummary())
                                        .thenReturn(new PerformanceMonitor.PerformanceSummary(50L, 1024L, 0, 1));

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.RUNTIME_ERROR, result.status());
                }

                @Test
                @DisplayName("Should handle null compiler gracefully")
                void shouldHandleNullCompiler() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("test").expectedOutput("test").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(null);

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
                        assertTrue(result.message().contains("not available"));
                }

                @Test
                @DisplayName("Should handle empty test case list")
                void shouldHandleEmptyTestCaseList() {
                        JudgeResult validationError = JudgeResult.builder()
                                        .status(SimpleResult.SYSTEM_ERROR)
                                        .message("Test cases cannot be empty")
                                        .build();

                        when(inputValidator.validate(anyString(), eq(JAVA), eq(Collections.emptyList()), anyLong(),
                                        anyLong()))
                                        .thenReturn(validationError);

                        JudgeResult result = judgeService.judge(Collections.emptyList(), VALID_CODE, JAVA,
                                        DEFAULT_TIME_LIMIT, DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.SYSTEM_ERROR, result.status());
                }
        }

        @Nested
        @DisplayName("Edge Case Tests")
        class EdgeCaseTests {

                @Test
                @DisplayName("Should handle null memory used in result")
                void shouldHandleNullMemoryUsedInResult() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("test").expectedOutput("test").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any(Path.class)))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult acceptedResult = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(null)
                                        .build();

                        when(executor.execute(any(), any(), any(), anyLong(), anyLong()))
                                        .thenReturn(acceptedResult);

                        JudgeResult summaryResult = JudgeResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(0)
                                        .build();
                        when(summarize.summarizeResults(anyList())).thenReturn(summaryResult);

                        when(performanceMonitor.getSummary())
                                        .thenReturn(new PerformanceMonitor.PerformanceSummary(50L, 0, 1, 0));

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                }

                @Test
                @DisplayName("Should handle first failure case correctly")
                void shouldHandleFirstFailureCase() throws Exception {
                        List<TestCase> testCases = List.of(
                                        TestCase.builder().input("1").expectedOutput("2").build(),
                                        TestCase.builder().input("2").expectedOutput("3").build());

                        when(inputValidator.validate(anyString(), eq(JAVA), anyList(), anyLong(), anyLong()))
                                        .thenReturn(null);

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any(Path.class)))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult wrongAnswer = CaseResult.builder()
                                        .status(SimpleResult.WRONG_ANSWER)
                                        .message("Output mismatch")
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .input("1")
                                        .expectedOutput("2")
                                        .actualOutput("1")
                                        .build();

                        CaseResult accepted = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .build();

                        when(executor.execute(any(), any(), any(), anyLong(), anyLong()))
                                        .thenReturn(wrongAnswer, accepted);

                        JudgeResult summaryResult = JudgeResult.builder()
                                        .status(SimpleResult.WRONG_ANSWER)
                                        .message("Wrong Answer")
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .caseResults(wrongAnswer)
                                        .build();
                        when(summarize.summarizeResults(anyList())).thenReturn(summaryResult);

                        when(performanceMonitor.getSummary())
                                        .thenReturn(new PerformanceMonitor.PerformanceSummary(50L, 1024L, 1, 1));

                        JudgeResult result = judgeService.judge(testCases, VALID_CODE, JAVA, DEFAULT_TIME_LIMIT,
                                        DEFAULT_MEMORY_LIMIT);

                        assertNotNull(result);
                        assertEquals(SimpleResult.WRONG_ANSWER, result.status());
                }
        }
}
